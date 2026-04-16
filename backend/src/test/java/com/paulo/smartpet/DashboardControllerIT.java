package com.paulo.smartpet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").exists())
                .andExpect(jsonPath("$.totalCustomers").exists())
                .andExpect(jsonPath("$.totalSales").exists())
                .andExpect(jsonPath("$.completedSales").exists())
                .andExpect(jsonPath("$.canceledSales").exists())
                .andExpect(jsonPath("$.stockValue").exists())
                .andExpect(jsonPath("$.lowStockCount").exists())
                .andExpect(jsonPath("$.salesAmountToday").exists())
                .andExpect(jsonPath("$.salesAmountWeek").exists())
                .andExpect(jsonPath("$.salesAmountMonth").exists())
                .andExpect(jsonPath("$.salesCountToday").exists())
                .andExpect(jsonPath("$.salesCountWeek").exists())
                .andExpect(jsonPath("$.salesCountMonth").exists());
    }
}