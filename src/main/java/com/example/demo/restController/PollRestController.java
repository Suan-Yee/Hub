package com.example.demo.restController;

import com.example.demo.dto.PollDto;
import com.example.demo.dto.PollOptionReportDto;
import com.example.demo.dto.UserDto;
import com.example.demo.dtoMapper.PollDtoMapper;
import com.example.demo.entity.Group;
import com.example.demo.entity.Poll;
import com.example.demo.entity.PollOption;
import com.example.demo.entity.User;
import com.example.demo.form.PollCreationForm;
import com.example.demo.form.UserVotedPollOption;
import com.example.demo.form.VoteRemoveForm;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PollRepository;
import com.example.demo.services.PollOptionService;
import com.example.demo.services.PollService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor

public class PollRestController {
    private final UserService userService;
    private final PollService pollService;
    private final PollOptionService pollOptionService;
    private final PollRepository pollRepository;
    private final PollDtoMapper pollDtoMapper;
    private final GroupRepository groupRepository;

    @PostMapping("/createPoll")
    public PollDto pollCreation(Principal principal, @RequestBody PollCreationForm pollCreationForm){
        User user = userService.findByStaffId(principal.getName());
        Poll poll = new Poll();
        LocalDateTime now = LocalDateTime.now();
        if(!pollCreationForm.getPollOptions().isEmpty() && pollCreationForm.getPollQuestion()!=null){
            poll.setDescription(pollCreationForm.getPollQuestion());
            poll.setUser(user);
            switch (pollCreationForm.getDuration()){
                case "1day"   : poll.setExpiredAt(now.plusDays(1));
                    break;
                case "2day"   : poll.setExpiredAt(now.plusDays(2));
                    break;
                case "3day"   : poll.setExpiredAt(now.plusDays(3));
                    break;
            }
            if(pollCreationForm.getGroupId()>0 || pollCreationForm.getGroupId() != 0L){
                Group group = groupRepository.findById(pollCreationForm.getGroupId()).orElse(null);
                if(group!=null)
                    poll.setGroup(group);
            }
            Poll pollObj =  pollService.save(poll);
            List<PollOption> savedPollOption = new ArrayList<>();

            if(!pollCreationForm.getPollOptions().isEmpty()){
                for (String option:pollCreationForm.getPollOptions()) {
                    PollOption pollOption = new PollOption();
                    pollOption.setName(option);
                    pollOption.setPoll(poll);
                    PollOption pollOption1 = pollOptionService.save(pollOption);
                    savedPollOption.add(pollOption1);
                }
            }
            pollObj.setPollOption(savedPollOption);
            PollDto savedPoll = pollRepository.findById(pollObj.getId()).map(pollDtoMapper::mapToPollViewObject).orElse(null);
            if(savedPoll!=null)
                return savedPoll;
            else return null;
        }
        return null;
    }
    @GetMapping("/get-all-poll")
    public ResponseEntity<List<PollDto>> getAllPost(){
        List<PollDto> polls = pollService.findAllPoll();
        if(polls!=null)
            return ResponseEntity.ok(polls);

        else return (ResponseEntity<List<PollDto>>) ResponseEntity.notFound();
    }
    @GetMapping("/check-answer-user-voted")
    public ResponseEntity<Map<String, Long>> checkAnswerUserVoted(@RequestParam long pollId, Principal principal){
        User user = userService.findByStaffId(principal.getName());
        Long optionIdUserVoted = null;
        if(user != null) {
            optionIdUserVoted = pollOptionService.findPollOptionIdByUserIdAndPollId(user.getId(), pollId);
        }
        Map<String, Long> response = new HashMap<>();
        response.put("optionIdUserVoted", optionIdUserVoted);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/save-user-voted-answer")
    public Boolean saveUserVotedAnswer(@RequestBody UserVotedPollOption userVotedPollOption
            ,Principal principal){
        User user = userService.findByStaffId(principal.getName());
        PollOption pollOption = pollOptionService.findPollOptionById(userVotedPollOption.getAnswerId());
        if(user!=null & pollOption !=null){
            if (!pollOptionService.checkUserHaveAlreadyVoted(userVotedPollOption.getAnswerId(),user.getId())) {
                pollOption.getUser().add(user);
                pollOptionService.save(pollOption);
                return true;
            }
        }
        return false;
    }

    @PostMapping("/remove-user-voted-answer")
    public ResponseEntity<?> removeUserVotedAnswer(@RequestBody VoteRemoveForm request, Principal principal) {
        User user = userService.findByStaffId(principal.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Map<String, Object> result = pollOptionService.deleteUserVotedOptionByAnswerIdAndUserId(
                request.getAnswerId(),
                user.getId(),
                request.getPollId()
        );
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body("Failed to remove vote");
        }
    }

    @GetMapping("/get-votes-for-poll-option")
    public ResponseEntity<List<UserDto>> getVotesForPollOption(@RequestParam("answerId") long optionId){
        List<UserDto> userList = pollOptionService.findUsersByPollOptionId(optionId);
        if(userList!=null)
            return ResponseEntity.ok(userList);
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/delete-poll")
    public Boolean deletePollId(@RequestBody PollDto pollDto){
        log.info("Poll Id {}",pollDto.getId());
        pollService.changeStatus(pollDto.getId());
        return true;
    }

    @GetMapping("/get-all-poll-by-groupId/{groupId}")
    public ResponseEntity<List<PollDto>> getAllPollByGroupId(@PathVariable("groupId") long groupId){
        List<PollDto> polls = pollService.findAllPollByGroupId(groupId);
        if(polls!=null){
            return ResponseEntity.ok(polls);
        }
        return ResponseEntity.notFound().build();
    }

}
