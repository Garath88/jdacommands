package commands.waifu;

import java.util.List;

public interface Roles {
    List<String> getRoleArguments();

    List<String> getRoles();

    String getRole(String argument);

    String getRoleRepresentation(String role);
}
