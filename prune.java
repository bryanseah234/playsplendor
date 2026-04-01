import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class prune {
    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("src/com/splendor/view/RemoteView.java")));
        
        // Let's replace the top to make it extend BaseTextView
        content = content.replace("implements IGameView", "extends BaseTextView");
        
        // Add the abstract implementations
        String abstractImpls = "\n    @Override\n    protected void sendOutput(String message) {\n        messageHandler.sendToClient(clientId, message);\n    }\n\n    @Override\n    protected String receiveInput(String prompt) {\n        sendOutput(prompt);\n        return messageHandler.waitForClientResponse(clientId, 120000);\n    }\n\n    @Override\n    protected void sendErrorLine(String errorMessage) {\n        messageHandler.sendToClient(clientId, Colors.colorize(\"ERROR: \" + errorMessage, Colors.RED));\n    }\n";
        
        content = content.replaceFirst("(this\\.messageHandler = messageHandler;\\s+\\})", "$1\n" + abstractImpls);
        
        // We will remove send(String message), waitForResponse(int timeout), and isBackToMenuInput(String input)
        // We remove the large promptForMove block and underneath it up to promptForTokenDiscard
        // Actually, we can use regexes to delete.
        content = content.replaceAll("(?s)/\\*\\*\\s+\\* Sends the rendered board-plus-menu.*?} // end buildMoveFromOption", "");
        
        // Maybe simpler to just write a python or node script to do it cleanly.
        Files.write(Paths.get("prune_out.txt"), content.getBytes());
    }
}
