package com.lesofn.archforge.server.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.infrastructure.auth.stp.StpWebUtil;
import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.file.FileStorageService;
import com.lesofn.archforge.user.api.domain.SysFile;
import com.lesofn.archforge.user.api.service.SysFileService;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Tests for {@code GET /web/file/{id}} visibility rules (public vs owner-only). */
@ExtendWith(MockitoExtension.class)
class WebFileControllerTest {

    private SysFileService sysFileService;
    private MockMvc mockMvc;
    private MockedStatic<StpWebUtil> stpWebUtil;

    @BeforeEach
    void setUp() {
        sysFileService = mock(SysFileService.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        lenient()
                .when(fileStorageService.download("p"))
                .thenAnswer(invocation -> new ByteArrayInputStream(new byte[] {
                        1
                }));
        WebFileController controller = new WebFileController(sysFileService, fileStorageService, new ArchForgeProperties());
        ReflectionTestUtils.setField(controller, "webPublicUrl", "http://localhost:8081");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void tearDown() {
        if (stpWebUtil != null) {
            stpWebUtil.close();
            stpWebUtil = null;
        }
        LoginContext.clearWebUser();
    }

    private void mockAnonymous() {
        stpWebUtil = mockStatic(StpWebUtil.class);
        stpWebUtil.when(StpWebUtil::isLogin).thenReturn(false);
    }

    @Test
    void publicVisibleFileDownloadsAnonymously() throws Exception {
        mockAnonymous();
        when(sysFileService.findById(1L)).thenReturn(Optional.of(imageFile(null, true)));

        mockMvc.perform(get("/web/file/1")).andExpect(status().isOk());
    }

    @Test
    void nonPublicFileReturns404ForAnonymousCaller() throws Exception {
        mockAnonymous();
        when(sysFileService.findById(2L)).thenReturn(Optional.of(imageFile(9L, false)));

        mockMvc.perform(get("/web/file/2")).andExpect(status().isNotFound());
    }

    @Test
    void nonPublicFileReturns404ForOtherLoggedInUser() throws Exception {
        LoginContext.setWebUser(7L, "alice");
        when(sysFileService.findById(2L)).thenReturn(Optional.of(imageFile(9L, false)));

        mockMvc.perform(get("/web/file/2")).andExpect(status().isNotFound());
    }

    @Test
    void nonPublicFileDownloadsForOwner() throws Exception {
        LoginContext.setWebUser(9L, "owner");
        when(sysFileService.findById(2L)).thenReturn(Optional.of(imageFile(9L, false)));

        mockMvc.perform(get("/web/file/2")).andExpect(status().isOk());
    }

    @Test
    void unknownFileReturns404() throws Exception {
        mockAnonymous();
        when(sysFileService.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/web/file/404")).andExpect(status().isNotFound());
    }

    @Test
    void uploadMarksFilePublicAndRecordsUploader() throws Exception {
        LoginContext.setWebUser(7L, "alice");
        when(sysFileService.create(any(SysFile.class)))
                .thenAnswer(invocation -> ((SysFile) invocation.getArgument(0)).setFileId(5L));
        MockMultipartFile multipartFile = new MockMultipartFile("file", "pic.png", "image/png", new byte[] {
                1
        });

        mockMvc.perform(multipart("/web/file/upload").file(multipartFile)).andExpect(status().isOk());

        ArgumentCaptor<SysFile> captor = ArgumentCaptor.forClass(SysFile.class);
        verify(sysFileService).create(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getPublicVisible());
        assertEquals(7L, captor.getValue().getCreatorId());
        assertTrue(captor.getValue().getStoragePath().endsWith(".png"));
    }

    private SysFile imageFile(Long creatorId, boolean publicVisible) {
        SysFile file = new SysFile()
                .setOriginalName("pic.png")
                .setStoragePath("p")
                .setContentType("image/png");
        file.setCreatorId(creatorId);
        file.setPublicVisible(publicVisible);
        return file;
    }
}
