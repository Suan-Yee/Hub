package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ExcelUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelUploadServiceImpl implements ExcelUploadService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updateUsers(MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Skip header row, start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                User user = parseUserFromRow(row);
                if (user != null) {
                    users.add(user);
                }
            }

            if (!users.isEmpty()) {
                userRepository.saveAll(users);
                log.info("Successfully uploaded {} users from Excel", users.size());
            }
        } catch (Exception e) {
            log.error("Failed to process Excel file", e);
            throw new ApiException("Failed to process Excel file: " + e.getMessage());
        }
    }

    private User parseUserFromRow(Row row) {
        try {
            String username = getCellValue(row.getCell(0));
            String email = getCellValue(row.getCell(1));
            String password = getCellValue(row.getCell(2));
            String bio = getCellValue(row.getCell(3));

            if (username == null || email == null || password == null) {
                log.warn("Skipping row with missing required fields");
                return null;
            }

            // Check if user already exists
            if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
                log.warn("User already exists: {}", username);
                return null;
            }

            return User.builder()
                    .username(username)
                    .email(email)
//                    .passwordHash(passwordEncoder.encode(password))
                    .bio(bio)
                    .isVerified(false)
                    .createdAt(OffsetDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error parsing row", e);
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }
}
