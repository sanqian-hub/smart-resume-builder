package com.srb.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.srb.backend.common.BaseResponse;
import com.srb.backend.model.dto.PageRequest;
import com.srb.backend.model.dto.ResumeAddRequest;
import com.srb.backend.model.dto.ResumeDeleteRequest;
import com.srb.backend.model.dto.ResumeProofreadRequest;
import com.srb.backend.model.dto.ResumeScoreRequest;
import com.srb.backend.model.dto.ResumeSelfIntroRequest;
import com.srb.backend.model.dto.ResumeUpdateRequest;
import com.srb.backend.model.dto.MatchRequest;
import com.srb.backend.model.entity.ResumeVersion;
import com.srb.backend.model.vo.ResumePublicShareVO;
import com.srb.backend.model.vo.ResumeProofreadVO;
import com.srb.backend.model.vo.ResumeScoreVO;
import com.srb.backend.model.vo.ResumeShareVO;
import com.srb.backend.model.vo.ResumeSelfIntroVO;
import com.srb.backend.model.vo.ResumeVersionSaveVO;
import com.srb.backend.model.vo.ResumeVO;
import com.srb.backend.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/add")
    public BaseResponse<Long> addResume(@Valid @RequestBody ResumeAddRequest resumeRequest, HttpServletRequest request) {
        Long resumeId = resumeService.addResume(request, resumeRequest);
        return BaseResponse.success(resumeId);
    }

    @PostMapping("/update")
    public BaseResponse<Void> updateResume(@Valid @RequestBody ResumeUpdateRequest resumeRequest, HttpServletRequest request) {
        resumeService.updateResume(request, resumeRequest);
        return BaseResponse.success(null);
    }

    @PostMapping("/delete")
    public BaseResponse<Void> deleteResume(@RequestBody ResumeDeleteRequest deleteRequest, HttpServletRequest request) {
        resumeService.deleteResume(request, deleteRequest.getId());
        return BaseResponse.success(null);
    }

    @GetMapping("/get/{id}")
    public BaseResponse<ResumeVO> getResume(@PathVariable Long id, HttpServletRequest request) {
        ResumeVO resumeVO = resumeService.getResume(request, id);
        return BaseResponse.success(resumeVO);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<ResumeVO>> pageResume(@RequestBody PageRequest pageRequest, HttpServletRequest request) {
        Page<ResumeVO> page = resumeService.pageResume(request, pageRequest.getCurrent(), pageRequest.getPageSize());
        return BaseResponse.success(page);
    }

    @GetMapping("/my/list")
    public BaseResponse<List<ResumeVO>> myList(HttpServletRequest request) {
        List<ResumeVO> list = resumeService.myList(request);
        return BaseResponse.success(list);
    }

    @PostMapping("/version/save")
    public BaseResponse<ResumeVersionSaveVO> saveVersion(@RequestParam Long resumeId,
                                                         @RequestParam(required = false) String remark,
                                                         HttpServletRequest request) {
        return BaseResponse.success(resumeService.saveVersion(request, resumeId, remark));
    }

    @GetMapping("/version/list")
    public BaseResponse<List<ResumeVersion>> listVersions(@RequestParam Long resumeId, HttpServletRequest request) {
        List<ResumeVersion> list = resumeService.listVersions(request, resumeId);
        return BaseResponse.success(list);
    }

    @PostMapping("/version/rollback")
    public BaseResponse<Void> rollbackVersion(@RequestParam Long versionId, HttpServletRequest request) {
        resumeService.rollbackVersion(request, versionId);
        return BaseResponse.success(null);
    }

    @PostMapping("/match")
    public BaseResponse<Map<String, Object>> matchAnalysis(@Valid @RequestBody MatchRequest matchRequest, HttpServletRequest request) {
        Map<String, Object> result = resumeService.matchAnalysis(request, matchRequest.getResumeId(), matchRequest.getJobDescription(), matchRequest.getModuleData());
        return BaseResponse.success(result);
    }

    @PostMapping("/score")
    public BaseResponse<ResumeScoreVO> scoreResume(@RequestBody ResumeScoreRequest scoreRequest, HttpServletRequest request) {
        ResumeScoreVO result = resumeService.scoreResume(request, scoreRequest.getResumeId(), scoreRequest.getModuleData());
        return BaseResponse.success(result);
    }

    @PostMapping("/proofread")
    public BaseResponse<ResumeProofreadVO> proofreadResume(@RequestBody ResumeProofreadRequest proofreadRequest, HttpServletRequest request) {
        ResumeProofreadVO result = resumeService.proofreadResume(request, proofreadRequest.getResumeId(), proofreadRequest.getModuleData());
        return BaseResponse.success(result);
    }

    @PostMapping("/share/create")
    public BaseResponse<String> createShare(@RequestParam Long resumeId,
                                            @RequestParam(required = false) Long versionId,
                                            @RequestParam(required = false) String password,
                                            @RequestParam(required = false) Integer expireDays,
                                            HttpServletRequest request) {
        return BaseResponse.success(resumeService.createShare(request, resumeId, versionId, password, expireDays));
    }

    @GetMapping("/share/list")
    public BaseResponse<List<ResumeShareVO>> listShares(@RequestParam Long resumeId, HttpServletRequest request) {
        return BaseResponse.success(resumeService.listShares(request, resumeId));
    }

    @PostMapping("/share/close")
    public BaseResponse<Void> closeShare(@RequestParam Long shareId, HttpServletRequest request) {
        resumeService.closeShare(request, shareId);
        return BaseResponse.success(null);
    }

    @PostMapping("/share/password")
    public BaseResponse<ResumeShareVO> updateSharePassword(@RequestParam Long shareId,
                                                           @RequestParam(required = false) String password,
                                                           HttpServletRequest request) {
        return BaseResponse.success(resumeService.updateSharePassword(request, shareId, password));
    }

    @PostMapping("/share/expire")
    public BaseResponse<ResumeShareVO> updateShareExpire(@RequestParam Long shareId,
                                                         @RequestParam(required = false) Integer expireDays,
                                                         HttpServletRequest request) {
        return BaseResponse.success(resumeService.updateShareExpire(request, shareId, expireDays));
    }

    @GetMapping("/share/public/{shareKey}")
    public BaseResponse<ResumePublicShareVO> getPublicShare(@PathVariable String shareKey,
                                                            HttpServletRequest request) {
        return BaseResponse.success(resumeService.getPublicShare(request, shareKey));
    }

    @PostMapping("/share/public/{shareKey}/verify")
    public BaseResponse<ResumeVO> verifyPublicShare(@PathVariable String shareKey,
                                                    @RequestParam String password) {
        return BaseResponse.success(resumeService.verifyPublicShare(shareKey, password));
    }

    @PostMapping("/self-intro")
    public BaseResponse<ResumeSelfIntroVO> generateSelfIntro(@Valid @RequestBody ResumeSelfIntroRequest selfIntroRequest,
                                                             HttpServletRequest request) {
        ResumeSelfIntroVO vo = resumeService.generateSelfIntro(
                request,
                selfIntroRequest.getResumeId(),
                selfIntroRequest.getDurationSeconds(),
                selfIntroRequest.getStyle(),
                selfIntroRequest.getJobDescription(),
                selfIntroRequest.getModuleData()
        );
        return BaseResponse.success(vo);
    }
}
