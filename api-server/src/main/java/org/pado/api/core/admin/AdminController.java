package org.pado.api.core.admin;

import java.util.List;


import org.pado.api.core.admin.dto.request.AdminSignupRequest;
import org.pado.api.core.admin.dto.response.AdminSignupResponse;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.domain.component.ComponentDefaultSetting;
import org.pado.api.domain.component.ComponentList;
import org.pado.api.domain.connection.Connection;
import org.pado.api.domain.user.User;
import org.pado.api.domain.user.UserRepository;
import org.pado.api.dto.request.ComponentCreateRequest;
import org.pado.api.dto.request.ComponentSettingRequest;
import org.pado.api.dto.request.ConnectionCreateRequest;
import org.pado.api.dto.request.CredentialRegisterRequest;
import org.pado.api.dto.request.ProjectCreateRequest;
import org.pado.api.dto.request.SigninRequest;
import org.pado.api.dto.request.SignupRequest;
import org.pado.api.dto.response.ComponentCreateResponse;
import org.pado.api.dto.response.ComponentDeleteResponse;
import org.pado.api.dto.response.ComponentListResponse;
import org.pado.api.dto.response.ComponentSettingResponse;
import org.pado.api.dto.response.ConnectionCreateResponse;
import org.pado.api.dto.response.ConnectionDeleteResponse;
import org.pado.api.dto.response.CredentialDeleteResponse;
import org.pado.api.dto.response.CredentialDetailResponse;
import org.pado.api.dto.response.CredentialResponse;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.dto.response.DeployStartResponse;
import org.pado.api.dto.response.DeployStopResponse;
import org.pado.api.dto.response.ProjectCreateResponse;
import org.pado.api.dto.response.ProjectDetailResponse;
import org.pado.api.dto.response.ProjectListResponse;
import org.pado.api.dto.response.SigninResponse;
import org.pado.api.dto.response.SignupResponse;
import org.pado.api.service.AuthService;
import org.pado.api.service.ComponentService;
import org.pado.api.service.CredentialService;
import org.pado.api.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/admin-test")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자용 API")
public class AdminController {

    private final AdminService adminService;

    /** ----------------- Auth ----------------- */
    @Operation(summary = "회원가입", description = "유저 정보를 기반으로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public AdminSignupResponse signup(@Valid @RequestBody AdminSignupRequest request) {
        return adminService.signup(request);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 시도합니다.")
    @PostMapping("/signin")
    public DefaultResponse signin(@RequestBody SigninRequest request) {
        // 실제 로그인 검증은 없고, 단순히 메시지만 반환
        return new DefaultResponse("로그인 성공");
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리를 수행합니다.")
    @PostMapping("/signout")
    public DefaultResponse signout() {
        return new DefaultResponse("로그아웃 성공");
    }

    /** ----------------- Credential ----------------- */
    @PostMapping("/{userId}/credentials")
    @Operation(
        summary = "크리덴셜 등록", 
        description = "새로운 크리덴셜을 등록합니다. 외부 API를 통해 유효성을 검증한 후 Vault에 안전하게 저장됩니다."
    )
    public CredentialResponse createCredential(
            @PathVariable Long userId,
            @RequestBody CredentialRegisterRequest request) {
        return adminService.createCredential(userId, request);
    }

    @Operation(
        summary = "크리덴셜 목록 조회", 
        description = "현재 사용자가 등록한 모든 크리덴셜의 메타데이터를 조회합니다. (실제 크리덴셜 데이터는 포함되지 않음)"
    )
    @GetMapping("/{userId}/credentials")
    public List<CredentialResponse> getAllCredentials(@PathVariable Long userId) {
        return adminService.getAllCredentials(userId);
    }

    @Operation(
        summary = "크리덴셜 개별 조회", 
        description = "특정 크리덴셜의 상세 정보를 조회합니다. Vault에서 실제 크리덴셜 데이터를 복호화하여 반환합니다."
    )
    @GetMapping("/{userId}/credentials/{credentialId}")
    public CredentialDetailResponse getCredential(@PathVariable Long userId, @PathVariable Long credentialId) {
        return adminService.getCredential(userId, credentialId);
    }
    
    @Operation(
        summary = "크리덴셜 삭제", 
        description = "크리덴셜을 삭제합니다. Vault와 DB에서 모두 제거됩니다."
    )
    @DeleteMapping("/{userId}/credentials/{credentialId}")
    public CredentialDeleteResponse deleteCredential(@PathVariable Long userId, @PathVariable Long credentialId) {
        return adminService.deleteCredential(userId, credentialId);
    }

    /** ----------------- Project ----------------- */
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    @PostMapping("/{userId}/projects")
    public ProjectCreateResponse createProject(@Valid @RequestBody ProjectCreateRequest request,
                                            @PathVariable Long userId) {
        return adminService.createProject(request, userId);
    }

    @Operation(summary = "프로젝트 목록 조회", description = "사용자의 프로젝트 목록을 조회합니다")
    @GetMapping("/{userId}/projects")
    public ProjectListResponse getProjects(@PathVariable Long userId) {
        return adminService.getProjects(userId);
    }

    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트의 상세 정보를 조회합니다.")
    @GetMapping("/{userId}/projects/{id}")
    public ProjectDetailResponse getProjectDetail(@PathVariable Long id, @PathVariable Long userId) {
        return adminService.getProjectDetail(id, userId);
    }
    
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    @DeleteMapping("/{userId}/projects/{id}")
    public DefaultResponse deleteProject(@PathVariable Long id, @PathVariable Long userId) {
        return adminService.deleteProject(id, userId);
    }

    /** ----------------- Component ----------------- */
    @Operation(summary = "컴포넌트 생성", description = "새로운 컴포넌트를 생성합니다.")
    @PostMapping("/{userId}/projects/{projectId}/components")
    public ComponentCreateResponse createComponent(@PathVariable Long projectId, @Valid @RequestBody ComponentCreateRequest request, @PathVariable Long userId) {
        return adminService.createComponent(projectId, request, userId);
    }

    @Operation(summary = "컴포넌트 목록 조회", description = "전체 컴포넌트의 목록을 조회합니다.")
    @GetMapping("/components")
    public ComponentListResponse getComponentList() {
        return adminService.getComponentList();
    }

    @Operation(summary = "컴포넌트 설정 변경", description = "컴포넌트의 설정을 변경합니다.")
    @PostMapping("/{userId}/projects/{projectId}/components/{componentId}/setting")
    public ComponentSettingResponse setComponentSetting(@PathVariable Long projectId, @PathVariable Long componentId, @RequestBody @Valid ComponentSettingRequest request, @PathVariable Long userId) {
        return adminService.setComponentSetting(projectId, componentId, request, userId);
    }

    @Operation(summary = "컴포넌트 삭제", description = "컴포넌트를 삭제합니다.")
    @DeleteMapping("/{userId}/projects/{projectId}/components/{componentId}")
    public ComponentDeleteResponse deleteComponent(@PathVariable Long projectId, @PathVariable Long componentId, @PathVariable Long userId) {
        return adminService.deleteComponent(projectId, componentId, userId);
    }

    @Operation(summary = "컴포넌트 연결", description = "컴포넌트 간 연결을 수립합니다.")
    @PostMapping("/{userId}/projects/{projectId}/components/{componentId}/connections")
    public ConnectionCreateResponse createConnection(@PathVariable Long projectId, @PathVariable Long componentId, @RequestBody @Valid ConnectionCreateRequest request, @PathVariable Long userId) {
        return adminService.createConnection(projectId, componentId, request, userId);
    }

    @Operation(summary = "컴포넌트 연결 삭제", description = "컴포넌트 간 연결을 삭제합니다.")
    @DeleteMapping("/{userId}/projects/{projectId}/components/{sourceComponentId}/connections/{connectionId}")
    public ConnectionDeleteResponse deleteConnection(@PathVariable Long projectId, @PathVariable Long sourceComponentId, @PathVariable Long connectionId, @PathVariable Long userId) {
        return adminService.deleteConnection(projectId, sourceComponentId, connectionId, userId);
    }

    /** ----------------- Deploy ----------------- */
    @Operation(summary = "프로젝트 배포", description = "새로운 프로젝트를 배포합니다.")
    @PostMapping("/{userId}/projects/{projectId}/deploy/start")
    public DeployStartResponse startProjectDeployment(@PathVariable Long projectId, @PathVariable Long userId) {
        return adminService.startDeployment(projectId, userId);
    }

    @Operation(summary = "프로젝트 배포 중지", description = "진행 중인 프로젝트 배포를 중지합니다.")
    @PostMapping("/{userId}/projects/{projectId}/deploy/stop")
    public DeployStopResponse stopProjectDeployment(@PathVariable Long projectId, @PathVariable Long userId) {
        return adminService.stopDeployment(projectId, userId);
    }

    /** ----------------- Component Default Setting ----------------- */
    @Operation(summary = "기본 설정 생성", description = "새로운 컴포넌트 기본 설정을 생성합니다.")
    @PostMapping("/components/default-settings")
    public ComponentDefaultSetting createDefaultSetting(@RequestBody ComponentDefaultSetting request) {
        return adminService.createDefaultSetting(request);
    }

    @Operation(summary = "기본 설정 목록 조회", description = "모든 컴포넌트 기본 설정을 조회합니다.")
    @GetMapping("/components/default-settings")
    public List<ComponentDefaultSetting> getAllDefaultSettings() {
        return adminService.getAllDefaultSettings();
    }

    @Operation(summary = "기본 설정 상세 조회", description = "ID를 기반으로 특정 컴포넌트 기본 설정을 조회합니다.")
    @GetMapping("/components/default-settings/{id}")
    public ComponentDefaultSetting getDefaultSetting(@PathVariable String id) {
        return adminService.getDefaultSetting(id);
    }

    @Operation(summary = "기본 설정 수정", description = "ID를 기반으로 특정 컴포넌트 기본 설정을 수정합니다.")
    @PutMapping("/components/default-settings/{id}")
    public ComponentDefaultSetting updateDefaultSetting(@PathVariable String id, @RequestBody ComponentDefaultSetting setting) {
        return adminService.updateDefaultSetting(id, setting);
    }

    @Operation(summary = "기본 설정 삭제", description = "ID를 기반으로 특정 컴포넌트 기본 설정을 삭제합니다.")
    @DeleteMapping("/components/default-settings/{id}")
    public DefaultResponse deleteDefaultSetting(@PathVariable String id) {
        return adminService.deleteDefaultSetting(id);
    }

    /** ----------------- Component List ----------------- */
    @Operation(summary = "컴포넌트 리스트 생성", description = "새로운 ComponentList를 생성합니다.")
    @PostMapping("/components/lists")
    public ComponentList createComponentList(@RequestBody ComponentList request) {
        return adminService.createComponentList(request);
    }

    @Operation(summary = "컴포넌트 리스트 상세 조회", description = "ID로 ComponentList를 조회합니다.")
    @GetMapping("/components/lists/{id}")
    public ComponentList getComponentListDetail(@PathVariable Long id) {
        return adminService.getComponentListDetail(id);
    }

    @Operation(summary = "컴포넌트 리스트 수정", description = "ID로 ComponentList를 수정합니다.")
    @PutMapping("/components/lists/{id}")
    public ComponentList updateComponentList(@PathVariable Long id, @RequestBody ComponentList request) {
        return adminService.updateComponentList(id, request);
    }

    @Operation(summary = "컴포넌트 리스트 삭제", description = "ID로 ComponentList를 삭제합니다.")
    @DeleteMapping("/components/lists/{id}")
    public DefaultResponse deleteComponentList(@PathVariable Long id) {
        return adminService.deleteComponentList(id);
    }


    /** ----------------- Connection ----------------- */
    @Operation(summary = "Connection 목록 조회", description = "특정 유저의 프로젝트 내 모든 Connection을 조회합니다.")
    @GetMapping("/{userId}/projects/{projectId}/connections")
    public List<Connection> getAllConnections(@PathVariable Long userId, @PathVariable Long projectId) {
        return adminService.getAllConnections(userId, projectId);
    }

    @Operation(summary = "Connection 상세 조회", description = "특정 유저의 특정 Connection을 조회합니다.")
    @GetMapping("/{userId}/projects/{projectId}/connections/{connectionId}")
    public Connection getConnection(@PathVariable Long userId, @PathVariable Long projectId, @PathVariable Long connectionId) {
        return adminService.getConnection(userId, projectId, connectionId);
    }

    @Operation(summary = "Connection 수정", description = "특정 유저의 특정 Connection을 수정합니다.")
    @PutMapping("/{userId}/projects/{projectId}/connections/{connectionId}")
    public Connection updateConnection(@PathVariable Long userId, @PathVariable Long projectId,
                                    @PathVariable Long connectionId, @RequestBody Connection request) {
        return adminService.updateConnection(userId, projectId, connectionId, request);
    }

    /** ----------------- 관리자 계정 사용 ----------------- */
    // private final AuthService authService;
    // private final CredentialService credentialService;
    // private final ProjectService projectService;
    // private final ComponentService componentService;
    // private final UserRepository userRepository;

    // // 하드코딩된 관리자 사용자 ID (실제로는 설정값으로 관리)
    // private static final Long ADMIN_USER_ID = 1L;

    // /**
    //  * 관리자용 User 객체 생성 헬퍼 메소드
    //  */
    // private CustomUserDetails createAdminUserDetails() {
    //     User adminUser = userRepository.findById(ADMIN_USER_ID)
    //         .orElseThrow(() -> new RuntimeException("관리자 계정을 찾을 수 없습니다. ID: " + ADMIN_USER_ID));
    //     return new CustomUserDetails(adminUser);
    // }
    
    // /**
    //  * Auth
    //  */
    // @Operation(summary = "회원가입", description = "유저 정보를 기반으로 회원가입을 진행합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "회원가입 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = SignupResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청 (유효성 검증 실패, 중복된 이메일 등)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "409",
    //         description = "이미 존재하는 사용자",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/signup")
    // public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    //     return ResponseEntity.ok(authService.signup(request));
    // }
    
    // @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 시도합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "로그인 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = SigninResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청 (필수 필드 누락 등)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패 (잘못된 이메일 또는 비밀번호)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "존재하지 않는 사용자",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/signin")
    // public ResponseEntity<SigninResponse> signin(@Valid @RequestBody SigninRequest request) {
    //     return ResponseEntity.ok(authService.signin(request));
    // }

    // @Operation(summary = "로그아웃", description = "로그아웃 처리를 수행합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "로그아웃 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청 (유효하지 않은 토큰 등)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증되지 않은 사용자 (토큰 만료 또는 무효)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/signout")
    // public ResponseEntity<DefaultResponse> signout(
    //         @RequestBody Map<String, String> request) {
        
    //     String refreshToken = request.get("refreshToken");
        
    //     return ResponseEntity.ok(authService.signout(createAdminUserDetails(), refreshToken));
    // }

    // /**
    //  * Credential
    //  */
    // @PostMapping("/credentials")
    // @Operation(
    //     summary = "크리덴셜 등록", 
    //     description = "새로운 크리덴셜을 등록합니다. 외부 API를 통해 유효성을 검증한 후 Vault에 안전하게 저장됩니다."
    // )
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "201",
    //         description = "크리덴셜 등록 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = CredentialResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "요청 데이터 오류 (필수 필드 누락, 잘못된 형식, 필드 길이 초과, 유효하지 않은 크리덴셜)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "409",
    //         description = "중복된 크리덴셜 이름",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류 (Vault 암호화 실패, DB 연결 오류)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // public ResponseEntity<CredentialResponse> createCredential(
    //         @RequestBody CredentialRegisterRequest request) {
    //             return ResponseEntity.ok(credentialService.createCredential(request, createAdminUserDetails()));
    // }

    // // 크리덴셜 전체 조회
    // @Operation(
    //     summary = "크리덴셜 목록 조회", 
    //     description = "현재 사용자가 등록한 모든 크리덴셜의 메타데이터를 조회합니다. (실제 크리덴셜 데이터는 포함되지 않음)"
    // )
    // @GetMapping("/credentials")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "크리덴셜 목록 조회 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = CredentialResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 오류 (로그인 필요)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // public ResponseEntity<List<CredentialResponse>> getAllCredentials() {
    //     return ResponseEntity.ok(credentialService.getAllCredentials(createAdminUserDetails()));
    // }

    // // 크리덴셜 개별 조회
    // @Operation(
    //     summary = "크리덴셜 개별 조회", 
    //     description = "특정 크리덴셜의 상세 정보를 조회합니다. Vault에서 실제 크리덴셜 데이터를 복호화하여 반환합니다."
    // )
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "크리덴셜 상세 조회 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = CredentialDetailResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청 (존재하지 않거나 유효하지 않은 credential ID, Vault 데이터 조회 실패)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 오류 (로그인 필요)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "403",
    //         description = "접근 권한 없음 (타 사용자 크리덴셜 접근 불가)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "리소스를 찾을 수 없음 (크리덴셜 없음, Vault 데이터 없음)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류 (Vault 서비스 내부 오류, 복호화 실패, DB 오류)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @GetMapping("/credentials/{credentialId}")
    // public ResponseEntity<CredentialDetailResponse> getCredential(@PathVariable Long credentialId) {
    //     return ResponseEntity.ok(credentialService.getCredential(createAdminUserDetails(), credentialId));
    // }
    
    // // 크리덴셜 삭제
    // @Operation(
    //     summary = "크리덴셜 삭제", 
    //     description = "크리덴셜을 삭제합니다. Vault와 DB에서 모두 제거됩니다."
    // )
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "204",
    //         description = "크리덴셜 삭제 성공"
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청 (컴포넌트에서 사용 중인 크리덴셜 삭제 시도)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 오류 (로그인 필요)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "403",
    //         description = "접근 권한 없음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "크리덴셜을 찾을 수 없음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "409",
    //         description = "사용 중인 크리덴셜 삭제 시도",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류 (Vault 삭제 실패 또는 서비스 내부 오류)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @DeleteMapping("/credentials/{credentialId}")
    // public ResponseEntity<CredentialDeleteResponse> deleteCredential(@PathVariable Long credentialId) {
    //     return ResponseEntity.ok(credentialService.deleteCredential(createAdminUserDetails(), credentialId));
    // }

    // /**
    //  * Project
    //  */
    // @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "프로젝트 생성 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ProjectCreateResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "409",
    //         description = "이미 존재하는 프로젝트 이름",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/projects")
    // public ResponseEntity<ProjectCreateResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
    //     return ResponseEntity.ok(projectService.createProject(request, createAdminUserDetails()));
    // }

    // @Operation(summary = "프로젝트 목록 조회", description = "사용자의 프로젝트 목록을 조회합니다")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "프로젝트 목록 조회 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ProjectListResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @GetMapping("/projects")
    // public ResponseEntity<ProjectListResponse> getProjects() {
    //     return ResponseEntity.ok(projectService.getProjects(createAdminUserDetails()));
    // }
    
    // @Operation(summary = "프로젝트 상세 조회", description = "프로젝트의 상세 정보를 조회합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "프로젝트 상세 조회 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ProjectDetailResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트를 찾을 수 없습니다.",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @GetMapping("/projects/{id}")
    // public ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable Long id) {
    //     return ResponseEntity.ok(projectService.getProjectDetail(id, createAdminUserDetails()));
    // }

    // @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "프로젝트 삭제 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트를 찾을 수 없습니다.",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @DeleteMapping("/projects/{id}")
    // public ResponseEntity<DefaultResponse> deleteProject(@PathVariable Long id) {
    //     return ResponseEntity.ok(projectService.deleteProject(id, createAdminUserDetails()));
    // }

    // /**
    //  * Component
    //  */
    // @Operation(summary = "컴포넌트 목록 조회", description = "전체 컴포넌트의 목록을 조회합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "컴포넌트 목록 조회 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ComponentListResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @GetMapping("/components")
    // public ResponseEntity<ComponentListResponse> getComponentList() {
    //     return ResponseEntity.ok(componentService.getComponentList());
    // }

    // @Operation(summary = "컴포넌트 생성", description = "새로운 컴포넌트를 생성합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "컴포넌트 생성 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ComponentCreateResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트 또는 컴포넌트가 존재하지 않음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/projects/{projectId}/components")
    // public ResponseEntity<ComponentCreateResponse> createComponent(@PathVariable Long projectId, @Valid @RequestBody ComponentCreateRequest request) {
    //     return ResponseEntity.ok(componentService.createComponent(projectId, request, createAdminUserDetails()));
    // }

    // @Operation(summary = "컴포넌트 설정 변경", description = "컴포넌트의 설정을 변경합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "컴포넌트 설정 변경 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ComponentSettingResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트 또는 컴포넌트가 존재하지 않음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/projects/{projectId}/components/{componentId}/setting")
    // public ResponseEntity<ComponentSettingResponse> setComponentSetting(@PathVariable Long projectId, @PathVariable Long componentId, @RequestBody @Valid ComponentSettingRequest request) {
    //     return ResponseEntity.ok(componentService.setComponentSetting(projectId, componentId, request, createAdminUserDetails()));
    // }
    

    // @Operation(summary = "컴포넌트 삭제", description = "컴포넌트를 삭제합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "컴포넌트 삭제 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ComponentSettingResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트 또는 컴포넌트가 존재하지 않음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @DeleteMapping("/projects/{projectId}/components/{componentId}")
    // public ResponseEntity<ComponentDeleteResponse> deleteComponent(@PathVariable Long projectId, @PathVariable Long componentId) {
    //     return ResponseEntity.ok(componentService.deleteComponent(projectId, componentId, createAdminUserDetails()));
    // }

    // @Operation(summary = "컴포넌트 연결", description = "컴포넌트 간 연결을 수립합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "컴포넌트 연결 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ConnectionCreateResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트 또는 컴포넌트가 존재하지 않음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/projects/{projectId}/components/{componentId}/connections")
    // public ResponseEntity<ConnectionCreateResponse> createConnection(@PathVariable Long projectId, @PathVariable Long componentId, @RequestBody @Valid ConnectionCreateRequest request) {
    //     return ResponseEntity.ok(componentService.createConnection(projectId, componentId, request, createAdminUserDetails()));
    // }

    // @Operation(summary = "컴포넌트 연결 삭제", description = "컴포넌트 간 연결을 삭제합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "컴포넌트 연결 삭제 성공",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = ConnectionDeleteResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "401",
    //         description = "인증 실패",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "404",
    //         description = "프로젝트 또는 컴포넌트가 존재하지 않음",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @DeleteMapping("/projects/{projectId}/components/{sourceComponentId}/connections/{connectionId}")
    // public ResponseEntity<ConnectionDeleteResponse> deleteConnection(@PathVariable Long projectId, @PathVariable Long sourceComponentId, @PathVariable Long connectionId) {
    //     return ResponseEntity.ok(componentService.deleteConnection(projectId, sourceComponentId, connectionId, createAdminUserDetails()));
    // }
}
