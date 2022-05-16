package com.booking.recruitment.hotel.controller;

import com.booking.recruitment.hotel.model.City;
import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.repository.CityRepository;
import com.booking.recruitment.hotel.repository.HotelRepository;
import com.booking.testing.SlowTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:data.sql")
@SlowTest
class HotelControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Autowired private HotelRepository repository;
  @Autowired private CityRepository cityRepository;

        @Test
        @DisplayName("When all hotels are requested then they are all returned")
        void allHotelsRequested() throws Exception {
                mockMvc
                                .perform(get("/hotel"))
                                .andExpect(status().is2xxSuccessful())
                                .andExpect(jsonPath("$", hasSize((int) repository.count())));
        }

        @Test
        @DisplayName("When a hotel creation is requested then it is persisted")
        void hotelCreatedCorrectly() throws Exception {
          City city =
              cityRepository
                  .findById(1L)
                  .orElseThrow(
                      () -> new IllegalStateException("Test dataset does not contain a city with ID 1!"));
          Hotel newHotel = Hotel.builder().setName("This is a test hotel").setCity(city).build();
      
          Long newHotelId =
              mapper
                  .readValue(
                      mockMvc
                          .perform(
                              post("/hotel")
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .content(mapper.writeValueAsString(newHotel)))
                          .andExpect(status().isCreated())
                          .andReturn()
                          .getResponse()
                          .getContentAsString(),
                      Hotel.class)
                  .getId();
      
          newHotel.setId(newHotelId); // Populate the ID of the hotel after successful creation
      
          assertThat(
              repository
                  .findById(newHotelId)
                  .orElseThrow(
                      () -> new IllegalStateException("New Hotel has not been saved in the repository")),
              equalTo(newHotel));
        }

        @Test
        @DisplayName("When hotel by id is requested then hotel is returned")
        void fetchHotelCorrectly() throws Exception {
                Hotel hotel = mapper.readValue(mockMvc
                                .perform(get("/hotel/4"))
                                .andExpect(status().is2xxSuccessful())
                                .andReturn()
                                .getResponse()
                                .getContentAsString(), Hotel.class);

                assertThat(hotel.getId(), equalTo(4L));
        }

        @Test
        @DisplayName("When hotel by invalid id is requested then return 404")
        void fetchHotelWhenHotelDoesNotExist() throws Exception {
                mockMvc
                                .perform(get("/hotel/400"))
                                .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("When hotel delete is requested then hotel must be soft deleted")
        void deleteHotelById() throws Exception {
                City city = cityRepository
                                .findById(1L)
                                .orElseThrow(
                                                () -> new IllegalStateException(
                                                                "Test dataset does not contain a city with ID 1!"));
                Hotel newHotel = Hotel.builder().setName("This is a test hotel").setCity(city).build();

                Long newHotelId = mapper
                                .readValue(
                                                mockMvc
                                                                .perform(
                                                                                post("/hotel")
                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                .content(mapper.writeValueAsString(
                                                                                                                newHotel)))
                                                                .andExpect(status().isCreated())
                                                                .andReturn()
                                                                .getResponse()
                                                                .getContentAsString(),
                                                Hotel.class)
                                .getId();
                mockMvc
                                .perform(delete(String.format("/hotel/%s", newHotelId)))
                                .andExpect(status().is2xxSuccessful());

                assertThat(repository.findById(newHotelId).isPresent(), equalTo(false));
        }

        @Test
        @DisplayName("When hotel delete by invalid id is requested then return 404")
        void deleteWhenHotelDoesNotExist() throws Exception {

                mockMvc
                                .perform(delete(String.format("/hotel/%s", 12233)))
                                .andExpect(status().is4xxClientError());
        }

}
