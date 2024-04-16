package moofarm.ssuckssuck.domain.character.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moofarm.ssuckssuck.domain.character.domain.Avatar;
import moofarm.ssuckssuck.domain.character.domain.Grade;
import moofarm.ssuckssuck.domain.character.domain.repository.AvatarRepository;
import moofarm.ssuckssuck.domain.character.presentation.dto.response.AddExperienceResponse;
import moofarm.ssuckssuck.domain.user.domain.User;
import moofarm.ssuckssuck.global.utils.user.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static moofarm.ssuckssuck.domain.character.domain.Grade.RADISH;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarService implements AvatarServiceUtils {

    private final AvatarRepository avatarRepository;
    private final UserUtils userUtils;

    // 아바타 생성
    @Override
    @Transactional
    public Avatar createAvatar() {
        Avatar avatar = Avatar.createAvatar();

        avatarRepository.save(avatar);

        return avatar;
    }

    // 경험치 증가 및 등급 업데이트 분리
    @Transactional
    public AddExperienceResponse addExperience(Integer count) {
        User user = userUtils.getUserFromSecurityContext();
        Avatar avatar = user.getAvatar();

        Integer point = calculateExpPointsToAdd(count);

        avatar.addExperience(count);

        updateGrade(avatar);

        return calculateExperienceNeededForNextGrade(user, avatar);
    }

    /**
     *
     * Todo : 경험치 계산 로직 구현
     */
    // 경험치 계산
    private Integer calculateExpPointsToAdd(Integer count) {
        return 0;
    }

    // 다음 등급까지 필요한 경험치 계산
    private AddExperienceResponse calculateExperienceNeededForNextGrade(User user, Avatar avatar) {
        Grade currentGrade = avatar.getGrade();
        Integer currentExperience = avatar.getExperience();
        Grade[] grades = Grade.values();

        int currentIndex = Arrays.asList(grades).indexOf(currentGrade);
        Grade nextGrade = grades[currentIndex + 1];

        int expToNextGrade = nextGrade.getRequiredExperience() - currentGrade.getRequiredExperience();
        int expDiffCurrGrade = currentExperience - currentGrade.getRequiredExperience();

        avatar.updateGradeExpInfo(expToNextGrade, expDiffCurrGrade);

        return new AddExperienceResponse(user.getUserInfo());
    }



    // 등급 변경
    private void updateGrade(Avatar avatar) {
        Integer experience = avatar.getExperience();
        Grade currentGrade = avatar.getGrade();
        Grade newGrade = Grade.findByExperience(experience);

        if (newGrade == RADISH) {
            avatar.resetRadishInfo();
        } else if (newGrade != currentGrade) {
            avatar.updateGrade(newGrade);
        }
    }
}
