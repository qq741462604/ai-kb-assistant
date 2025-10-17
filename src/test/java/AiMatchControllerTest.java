import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AiMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @Test
//    public void testAiMatchEndpoint() throws Exception {
//        String jsonFields = "[\"tradeNo\",\"orderId\",\"txnAmt\"]";
//
//        MvcResult result = mockMvc.perform(post("/field/aiMatch")
//                        .param("persistPending", "true")
//                        .param("createdBy", "dev")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonFields))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String resp = result.getResponse().getContentAsString();
//        System.out.println("AI Match Response: " + resp);
//
//        // 可进一步用 Jackson 解析 resp 验证返回结构
//    }
}
