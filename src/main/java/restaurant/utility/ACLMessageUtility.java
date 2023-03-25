package restaurant.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import jade.lang.acl.ACLMessage;

@UtilityClass
public class ACLMessageUtility {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static <T> T getContent(ACLMessage message, Class<T> clazz) {
        return objectMapper.readValue(message.getContent(), clazz);
    }
}