package com.app.musicstore.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for validating image uploads
 * Provides comprehensive validation for file uploads including:
 * - File type validation
 * - File size validation
 * - Image dimension validation
 * - Security validation (magic number checking)
 */
public class ImageValidationUtil {

    // Allowed image MIME types
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg", 
            "image/png",
            "image/gif",
            "image/webp"
    );

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    // File size limits (in bytes)
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_PROFILE_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB for profile images
    
    // Image dimension limits
    public static final int MAX_IMAGE_WIDTH = 4000;
    public static final int MAX_IMAGE_HEIGHT = 4000;
    public static final int MIN_IMAGE_WIDTH = 100;
    public static final int MIN_IMAGE_HEIGHT = 100;
    
    // Profile image dimension limits
    public static final int MAX_PROFILE_WIDTH = 2000;
    public static final int MAX_PROFILE_HEIGHT = 2000;
    public static final int MIN_PROFILE_WIDTH = 50;
    public static final int MIN_PROFILE_HEIGHT = 50;

    /**
     * Validates a single image file
     * @param file The MultipartFile to validate
     * @param isProfileImage Whether this is a profile image (different size limits)
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateImageFile(MultipartFile file, boolean isProfileImage) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.error("No file provided");
        }

        // Check file size
        long maxSize = isProfileImage ? MAX_PROFILE_IMAGE_SIZE : MAX_FILE_SIZE;
        if (file.getSize() > maxSize) {
            String sizeLimit = isProfileImage ? "5MB" : "10MB";
            return ValidationResult.error("File size exceeds " + sizeLimit + " limit");
        }

        // Check file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return ValidationResult.error("File name is invalid");
        }

        // Check file extension
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult.error("File type not supported. Allowed types: JPG, PNG, GIF, WEBP");
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return ValidationResult.error("Invalid file type. Please upload a valid image file");
        }

        // Validate image dimensions and content
        try {
            ValidationResult dimensionResult = validateImageDimensions(file, isProfileImage);
            if (!dimensionResult.isValid()) {
                return dimensionResult;
            }

            // Additional security validation
            ValidationResult securityResult = validateImageSecurity(file);
            if (!securityResult.isValid()) {
                return securityResult;
            }

        } catch (IOException e) {
            return ValidationResult.error("Unable to process image file: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * Validates multiple image files
     * @param files List of MultipartFile to validate
     * @param isProfileImage Whether these are profile images
     * @param maxFiles Maximum number of files allowed
     * @return ValidationResult containing validation status and error message
     */
    public static ValidationResult validateImageFiles(List<MultipartFile> files, boolean isProfileImage, int maxFiles) {
        if (files == null || files.isEmpty()) {
            return ValidationResult.error("No files provided");
        }

        if (files.size() > maxFiles) {
            return ValidationResult.error("Too many files. Maximum " + maxFiles + " files allowed");
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                continue; // Skip empty files
            }

            ValidationResult result = validateImageFile(file, isProfileImage);
            if (!result.isValid()) {
                return ValidationResult.error("File " + (i + 1) + ": " + result.getErrorMessage());
            }
        }

        return ValidationResult.success();
    }

    /**
     * Validates image dimensions
     * @param file The image file
     * @param isProfileImage Whether this is a profile image
     * @return ValidationResult
     */
    private static ValidationResult validateImageDimensions(MultipartFile file, boolean isProfileImage) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        
        if (image == null) {
            return ValidationResult.error("Invalid image file - unable to read image data");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        int maxWidth = isProfileImage ? MAX_PROFILE_WIDTH : MAX_IMAGE_WIDTH;
        int maxHeight = isProfileImage ? MAX_PROFILE_HEIGHT : MAX_IMAGE_HEIGHT;
        int minWidth = isProfileImage ? MIN_PROFILE_WIDTH : MIN_IMAGE_WIDTH;
        int minHeight = isProfileImage ? MIN_PROFILE_HEIGHT : MIN_IMAGE_HEIGHT;

        if (width < minWidth || height < minHeight) {
            return ValidationResult.error(String.format("Image dimensions too small. Minimum: %dx%d pixels", minWidth, minHeight));
        }

        if (width > maxWidth || height > maxHeight) {
            return ValidationResult.error(String.format("Image dimensions too large. Maximum: %dx%d pixels", maxWidth, maxHeight));
        }

        return ValidationResult.success();
    }

    /**
     * Validates image security (magic number checking)
     * @param file The image file
     * @return ValidationResult
     */
    private static ValidationResult validateImageSecurity(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        
        if (fileBytes.length < 4) {
            return ValidationResult.error("File too small to be a valid image");
        }

        // Check magic numbers for common image formats
        String contentType = file.getContentType().toLowerCase();
        
        if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
            if (!isJPEG(fileBytes)) {
                return ValidationResult.error("File content does not match JPEG format");
            }
        } else if (contentType.equals("image/png")) {
            if (!isPNG(fileBytes)) {
                return ValidationResult.error("File content does not match PNG format");
            }
        } else if (contentType.equals("image/gif")) {
            if (!isGIF(fileBytes)) {
                return ValidationResult.error("File content does not match GIF format");
            }
        } else if (contentType.equals("image/webp")) {
            if (!isWebP(fileBytes)) {
                return ValidationResult.error("File content does not match WebP format");
            }
        }

        return ValidationResult.success();
    }

    /**
     * Checks if file is a valid JPEG by magic number
     */
    private static boolean isJPEG(byte[] fileBytes) {
        return fileBytes.length >= 2 && 
               fileBytes[0] == (byte) 0xFF && 
               fileBytes[1] == (byte) 0xD8;
    }

    /**
     * Checks if file is a valid PNG by magic number
     */
    private static boolean isPNG(byte[] fileBytes) {
        return fileBytes.length >= 8 &&
               fileBytes[0] == (byte) 0x89 &&
               fileBytes[1] == 0x50 && // P
               fileBytes[2] == 0x4E && // N
               fileBytes[3] == 0x47 && // G
               fileBytes[4] == 0x0D && // CR
               fileBytes[5] == 0x0A && // LF
               fileBytes[6] == 0x1A && // SUB
               fileBytes[7] == 0x0A;   // LF
    }

    /**
     * Checks if file is a valid GIF by magic number
     */
    private static boolean isGIF(byte[] fileBytes) {
        return fileBytes.length >= 6 &&
               fileBytes[0] == 0x47 && // G
               fileBytes[1] == 0x49 && // I
               fileBytes[2] == 0x46 && // F
               fileBytes[3] == 0x38 && // 8
               (fileBytes[4] == 0x37 || fileBytes[4] == 0x39) && // 7 or 9
               fileBytes[5] == 0x61;   // a
    }

    /**
     * Checks if file is a valid WebP by magic number
     */
    private static boolean isWebP(byte[] fileBytes) {
        return fileBytes.length >= 12 &&
               fileBytes[0] == 0x52 && // R
               fileBytes[1] == 0x49 && // I
               fileBytes[2] == 0x46 && // F
               fileBytes[3] == 0x46 && // F
               fileBytes[8] == 0x57 && // W
               fileBytes[9] == 0x45 && // E
               fileBytes[10] == 0x42 && // B
               fileBytes[11] == 0x50;   // P
    }

    /**
     * Gets file extension from filename
     */
    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Formats file size in human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Inner class to represent validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
