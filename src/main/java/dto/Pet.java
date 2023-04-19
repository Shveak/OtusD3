
package dto;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(exclude = "photoUrls")
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Pet {

    private String id;
    private String name;
    private List<String> photoUrls;
    private Category category;
    private List<TagPet> tags;
    private Status status;
}
