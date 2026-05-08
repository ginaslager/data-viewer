package com.dataviewer.controller;

import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import com.dataviewer.service.DatabaseDataStore;
import com.dataviewer.service.FileDataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataController.class)
class DataControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean FileDataStore fileStore;
    @MockBean DatabaseDataStore dbStore;

    @Test
    void queryReturnsEmptyPageWhenNoDataLoaded() throws Exception {
        when(dbStore.hasData()).thenReturn(false);
        when(fileStore.hasData()).thenReturn(false);

        mockMvc.perform(post("/api/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"page\":0,\"size\":50}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void queryDelegatesToFileStoreWhenLoaded() throws Exception {
        FlatRow row = FlatRow.builder().roofvogelName("Havik").build();
        PageResult<FlatRow> result = new PageResult<>(List.of(row), 1, 1, 0);

        when(dbStore.hasData()).thenReturn(false);
        when(fileStore.hasData()).thenReturn(true);
        when(fileStore.query(any())).thenReturn(result);

        mockMvc.perform(post("/api/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"page\":0,\"size\":50}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].roofvogelName").value("Havik"));
    }

    @Test
    void queryDelegatesToDbStoreWhenLoaded() throws Exception {
        FlatRow row = FlatRow.builder().roofvogelName("Arend").build();
        PageResult<FlatRow> result = new PageResult<>(List.of(row), 1, 1, 0);

        when(dbStore.hasData()).thenReturn(true);
        when(dbStore.query(any())).thenReturn(result);

        mockMvc.perform(post("/api/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"page\":0,\"size\":50}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].roofvogelName").value("Arend"));
    }

    @Test
    void queryReturnsBadRequestForInvalidPageSize() throws Exception {
        when(dbStore.hasData()).thenReturn(false);
        when(fileStore.hasData()).thenReturn(false);

        mockMvc.perform(post("/api/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"page\":0,\"size\":9999}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void queryReturnsBadRequestForInvalidFilterOperator() throws Exception {
        when(dbStore.hasData()).thenReturn(false);
        when(fileStore.hasData()).thenReturn(false);

        String body = """
            {
              "page": 0, "size": 50,
              "filters": [{"field":"roofvogelName","operator":"INVALID","value":"test"}]
            }
            """;

        mockMvc.perform(post("/api/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}
