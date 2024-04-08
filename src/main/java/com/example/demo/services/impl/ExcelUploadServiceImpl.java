package com.example.demo.services.impl;

import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.ExcelUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor @Slf4j
public class ExcelUploadServiceImpl implements ExcelUploadService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public boolean isExcelValid(MultipartFile file) {
        return Objects.equals(file.getContentType(),"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Override
    public List<User> getUserData(InputStream inputStream) {
        List<User> userList = new ArrayList<>();

        try{
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("Employee_Data");

            int rowIndex = 0;
            for(Row row : sheet){
                if(rowIndex < 3 ){
                    rowIndex++;
                    continue;
                }
                Iterator<Cell> cellIterator = row.iterator();

                int cellIndex = 0;
                User user = new User();
                while (cellIterator.hasNext()){
                    Cell cell = cellIterator.next();
                    switch (cellIndex){
                        case 0 : break;
                        case 1 :
                            user.setDivision(cell.getStringCellValue());
                            break;
                        case 2 :
                            user.setStaffId(cell.getStringCellValue());
                            break;
                        case 3 :
                            user.setName(cell.getStringCellValue());
                            break;
                        case 4 : {
                            if(cell.getCellType() == CellType.NUMERIC){
                                user.setDoor_log_number(String.valueOf((int) cell.getNumericCellValue()));
                            }else{
                                user.setDoor_log_number(cell.getStringCellValue());
                            }
                        }
                        break;
                        case 5 : user.setDepartment(cell.getStringCellValue());
                            break;
                        case 6 : user.setTeam(cell.getStringCellValue());
                            break;
                        case 7 : user.setEmail(cell.getStringCellValue());
                            break;
                    }
                    cellIndex ++ ;
                }
                userList.add(user);
            }
        }catch (IOException e){
            throw new ApiException(e.getMessage());
        }
        return userList;
    }
}


