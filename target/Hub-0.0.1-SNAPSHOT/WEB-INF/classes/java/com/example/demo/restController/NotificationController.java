package com.example.demo.restController;

import com.example.demo.dto.NotificationDto;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.services.NotificationService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/noti")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> fetchAllNoti(@RequestParam(name = "page",defaultValue = "0")int page,
                                          @RequestParam(name = "size",defaultValue = "4")int size){

        Page<NotificationDto> notiList = notificationService.fetchAllNotificationByRecipient(page,size, Sort.by(Sort.Direction.DESC,"time"));
        log.info("NotiList {}",notiList);
        if(notiList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(notiList,HttpStatus.OK);
        }
    }
    @PostMapping ("/changeToReadStatus")
    public ResponseEntity<?> changeStatus(@RequestBody NotificationDto notificationDto){
        log.info("Notification ID {}",notificationDto.getId());
        boolean status = notificationService.changeStatusToRead(notificationDto.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/totalNotiCount")
    public ResponseEntity<?> getNotificationCount(){
        Long count = notificationService.totalNotification();
        return new ResponseEntity<>(count,HttpStatus.OK);
    }
    @DeleteMapping("/deleteAllNotification")
    public ResponseEntity<?> deleteAllNotification(){
        notificationService.deleteAllNotification();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/turnToggleNotification")
    public void turnToggleNoti(Principal principal){
        User user = userService.findAuthenticatedUser(principal);
        userService.turnToggleNoti(user.getId());
    }

}
