package nextstep.subway.member.application;

import nextstep.subway.common.NotFoundException;
import nextstep.subway.favorite.application.FavoriteService;
import nextstep.subway.member.domain.Member;
import nextstep.subway.member.domain.MemberRepository;
import nextstep.subway.member.dto.MemberRequest;
import nextstep.subway.member.dto.MemberResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    private final FavoriteService favoriteService;

    public MemberService(MemberRepository memberRepository, FavoriteService favoriteService) {
        this.memberRepository = memberRepository;
        this.favoriteService = favoriteService;
    }

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        Member member = memberRepository.save(request.toMember());
        return MemberResponse.of(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse findMember(Long id) {
        Member member = findMemberById(id);
        return MemberResponse.of(member);
    }

    @Transactional
    public void updateMember(Long id, MemberRequest param) {
        Member member = findMemberById(id);
        member.update(param.toMember());
    }

    @Transactional
    public void deleteMember(Long id) {
        favoriteService.deleteFavoriteByMemberId(id);
        memberRepository.deleteById(id);
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    }
}
