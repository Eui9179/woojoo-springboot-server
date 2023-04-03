package leui.woojoo.domain.today_games.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import leui.woojoo.domain.today_games.dto.CreateTodayGameRequest;
import leui.woojoo.domain.today_games.dto.TodayGamesResponse;
import leui.woojoo.domain.today_games.service.TodayGamesService;
import leui.woojoo.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/games/todays")
public class TodayGamesController {

    private final TodayGamesService todayGamesService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public TodayGamesResponse getTodayGames(Principal principal) {
        // TODO 내친구들것만
        Long userId = UserUtils.resolveUserId(principal);
        return new TodayGamesResponse(todayGamesService.findAllByToday(userId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public String createTodayGames(Principal principal, @RequestBody CreateTodayGameRequest request) throws FirebaseMessagingException {
        Long userId = UserUtils.resolveUserId(principal);
        todayGamesService.save(userId, request);
        return "ok";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{todayGameId}")
    public String deleteTodayGame(@PathVariable Long todayGameId) {
        todayGamesService.deleteById(todayGameId);
        return "ok";
    }
}