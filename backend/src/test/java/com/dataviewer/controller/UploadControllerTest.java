package com.dataviewer.controller;

import com.dataviewer.service.UploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UploadController.class)
class UploadControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean UploadService uploadService;

    @Test
    void uploadReturnsOkWithServiceResult() throws Exception {
        Map<String, Object> serviceResult = Map.of("status", "ok", "mode", "file", "roofvogels", 2L);
        when(uploadService.upload(any(), eq("file"))).thenReturn(serviceResult);

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.xml", "application/xml", "<roofvogels/>".getBytes());

        mockMvc.perform(multipart("/api/upload").file(file).param("mode", "file"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.roofvogels").value(2));
    }

    @Test
    void uploadPropagatesServiceException() throws Exception {
        when(uploadService.upload(any(), any())).thenThrow(new IllegalArgumentException("Niet-ondersteund bestandstype: test.txt"));

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "data".getBytes());

        mockMvc.perform(multipart("/api/upload").file(file).param("mode", "file"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}
