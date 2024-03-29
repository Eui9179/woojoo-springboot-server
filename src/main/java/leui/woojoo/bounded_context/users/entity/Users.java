package leui.woojoo.bounded_context.users.entity;

import jakarta.persistence.*;
import leui.woojoo.bounded_context.BaseTimeEntity;
import leui.woojoo.bounded_context.games.entity.Games;
import leui.woojoo.bounded_context.groups.entity.Groups;
import leui.woojoo.bounded_context.users.dto.UserSimple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Users extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 20, nullable = false)
    private String phoneNumber;

    @Column(length = 200)
    private String profileImageName;

    @Column(length = 200)
    private String fcmToken;

    @ManyToMany(cascade = CascadeType.REMOVE)
    Set<Users> friends = new HashSet<>();

    @ManyToMany(cascade = CascadeType.REMOVE)
    Set<Users> blocklist = new HashSet<>();

    @OneToMany(mappedBy = "users", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Games> games = new ArrayList<>();

    @OneToOne(mappedBy = "users", cascade = CascadeType.REMOVE)
    private Groups group;

    public void asyncFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public UserSimple toProfile() {
        return UserSimple.builder()
                .id(id)
                .name(name)
                .profileImageName(profileImageName)
                .build();
    }

    public void updateProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
    }

    public void updateUserName(String name) {
        this.name = name;
    }

    public void addGames(Games game) {
        games.add(game);
        game.setUsers(this);
    }

    public void deleteGames(Games game) {
        games.remove(game);
        game.setUsers(null);
    }

    public void addGameList(List<Games> games) {
        this.games.addAll(games);
        for (Games game : games) {
            game.setUsers(this);
        }
    }

    public void deleteGameList(List<Games> gameList) {
        games.removeAll(gameList);
    }

    public void addFriend(Users friend) {
        this.friends.add(friend);
    }

    public void clearRelationship() {
        for (Users friend : this.getFriends()) {
            friend.getFriends().remove(this);
        }
        this.getFriends().clear();
    }

}
