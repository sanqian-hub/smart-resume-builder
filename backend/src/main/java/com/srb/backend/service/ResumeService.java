package com.srb.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.srb.backend.model.dto.ResumeAddRequest;
import com.srb.backend.model.vo.ResumeProofreadVO;
import com.srb.backend.model.vo.ResumeScoreVO;
import com.srb.backend.model.vo.ResumeSelfIntroVO;
import com.srb.backend.model.dto.ResumeUpdateRequest;
import com.srb.backend.model.vo.ResumePublicShareVO;
import com.srb.backend.model.vo.ResumeVersionSaveVO;
import com.srb.backend.model.vo.ResumeShareVO;
import com.srb.backend.model.vo.ResumeVO;
import com.srb.backend.model.entity.ResumeVersion;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ResumeService {
    Long addResume(HttpServletRequest request, ResumeAddRequest resumeRequest);
    void updateResume(HttpServletRequest request, ResumeUpdateRequest resumeRequest);
    void deleteResume(HttpServletRequest request, Long resumeId);
    ResumeVO getResume(HttpServletRequest request, Long resumeId);
    Page<ResumeVO> pageResume(HttpServletRequest request, int current, int pageSize);
    List<ResumeVO> myList(HttpServletRequest request);
    ResumeVersionSaveVO saveVersion(HttpServletRequest request, Long resumeId, String remark);
    List<ResumeVersion> listVersions(HttpServletRequest request, Long resumeId);
    void rollbackVersion(HttpServletRequest request, Long versionId);
    Map<String, Object> matchAnalysis(HttpServletRequest request, Long resumeId, String jobDescription, Map<String, String> moduleData);
    ResumeScoreVO scoreResume(HttpServletRequest request, Long resumeId, Map<String, String> moduleData);
    ResumeProofreadVO proofreadResume(HttpServletRequest request, Long resumeId, Map<String, String> moduleData);
    String createShare(HttpServletRequest request, Long resumeId, Long versionId, String password, Integer expireDays);
    List<ResumeShareVO> listShares(HttpServletRequest request, Long resumeId);
    void closeShare(HttpServletRequest request, Long shareId);
    ResumeShareVO updateSharePassword(HttpServletRequest request, Long shareId, String password);
    ResumeShareVO updateShareExpire(HttpServletRequest request, Long shareId, Integer expireDays);
    ResumePublicShareVO getPublicShare(HttpServletRequest request, String shareKey);
    ResumeVO verifyPublicShare(String shareKey, String password);
    ResumeSelfIntroVO generateSelfIntro(HttpServletRequest request, Long resumeId, Integer durationSeconds, String style, String jobDescription, Map<String, String> moduleData);
}
