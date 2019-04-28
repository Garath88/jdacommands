package utils;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.ReceivedMessage;

public final class PrivateChannelWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateChannelWrapper.class);

    private PrivateChannelWrapper() {
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

    public static <T> Consumer<T> userIsInGuild(
        ThrowingConsumer<T, Exception> throwingConsumer) {

        return ret -> {
            try {
                if (ret instanceof ReceivedMessage) {
                    ReceivedMessage msg = (ReceivedMessage)ret;
                    MessageChannel chan = msg.getChannel();
                    if (chan instanceof PrivateChannelImpl) {
                        PrivateChannelImpl pc = (PrivateChannelImpl)chan;
                        if (userIsInGuild(pc.getUser())) {
                            throwingConsumer.accept(ret);
                        }
                    }
                } else {
                    throwingConsumer.accept(ret);
                }
            } catch (Exception e) {
                LOGGER.warn("DM error: {}", e.getMessage());
            }
        };
    }

    private static boolean userIsInGuild(User user) {
        Member member = GuildUtil.getGuild(user.getJDA()).getMemberById(user.getId());
        return member != null;
    }
}
