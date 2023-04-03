package leui.woojoo.domain.sms;

import leui.woojoo.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SmsService {

    private final SmsRepository smsRepository;

    public void save(String phoneNumber, String cp) {
        smsRepository.save(Sms.builder()
                .phoneNumber(phoneNumber)
                .cp(cp)
                .build());
    }

    @Transactional
    public void delete(String phoneNumber) {
        smsRepository.deleteByPhoneNumber(phoneNumber);
    }

    public boolean verify(String phoneNumber, String cp) {
        Sms sms = smsRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new DataNotFoundException("해당 유저가 없습니다."));
        return sms.getCp().equals(cp);
    }
}
