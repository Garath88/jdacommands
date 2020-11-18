/*
 * Copyright 2017 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package commands.examples;

import java.util.Random;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import utils.ArgumentChecker;

public class ChooseCommand extends Command {

    public ChooseCommand() {
        this.name = "choose";
        this.help = "make a decision.";
        this.arguments = "<item> <item> ...";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String arguments = event.getArgs();
            ArgumentChecker.checkArgsBySpaceRequires(arguments, 2);
            String[] items = arguments.split("\\s+");
            event.replySuccess("I choose `" + items[new Random().nextInt(items.length)] + "`");
        } catch (IllegalArgumentException e) {
            event.replyWarning(e.getMessage());
        }
    }
}
