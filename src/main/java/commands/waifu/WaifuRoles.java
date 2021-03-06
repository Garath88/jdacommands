package commands.waifu;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.CaseFormat;

public class WaifuRoles implements Roles {

    enum Waifus {
        ALICE("Team Alice Walkure", "Team Alice"),
        ANNEROSE("Team Annerose"),
        ASAGI("Team Asagi"),
        ASUKA("Team Asuka"),
        BEATRICE("Team Beatrice Kushan", "Team Beatrice"),
        FUTABA("Futaba · Lily · Ramses", "Team Futaba"),
        IERI("Team Rieri Bishop", "Team Rieri"),
        INGRID("Team Ingrid"),
        KANA("Team Mama Kana", "Team Kana"),
        KIRA("Team Kila Kushan", "Team Kila"),
        KILA("Team Kila Kushan", "Team Kila"),
        KURENAI("Team Kurenai"),
        MAYA("Team Maya Cordelia", "Team Maya"),
        MURASAKI("Team Murasaki"),
        MUU("Team Murasaki"),
        NAOMI("Team Naomi Evans", "Team Naomi"),
        OBORO("Oboro Squad"),
        RINKO("Rinko is #1"),
        AKIYAMA("Rinko is #1"),
        SAIKA("Team Fuuma Saika", "Team Saika"),
        SAKURA("Sakura's Harbingers"),
        SHIRANUI("Team Mamakaze"),
        SHIZURU("Team Shizuru"),
        KALYA("Team Snake Lady", "Team SnakeLady"),
        SNAKE("Team Snake Lady", "Team SnakeLady"),
        TOKIKO("Team Tokiko"),
        YUKI("Team Yukikaze", "Team Yuki"),
        ZAIDAN("Zaidan's Interns"),
        ;

        private final String roleName;
        private final String teamName;

        Waifus(String teamName) {
            this.roleName = teamName;
            this.teamName = teamName;
        }

        Waifus(String roleName, String teamName) {
            this.roleName = roleName;
            this.teamName = teamName;
        }

        String getRoleName() {
            return this.roleName;
        }

        String getTeamName() {
            return teamName;
        }

        @Override
        public String toString() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
        }
    }

    @Override
    public List<String> getRoleArguments() {
        return Stream.of(Waifus.values())
            .map(Waifus::toString)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoles() {
        return Stream.of(Waifus.values())
            .map(Waifus::getRoleName)
            .collect(Collectors.toList());
    }

    @Override
    public String getRole(String argument) {
        return Waifus.valueOf(argument.toUpperCase()).getRoleName();
    }

    @Override
    public String getRoleRepresentation(String role) {
        return Stream.of(Waifus.values())
            .filter(name -> name.getRoleName().equals(role))
            .findFirst()
            .map(Waifus::getTeamName)
            .orElseThrow(IllegalStateException::new);
    }
}
