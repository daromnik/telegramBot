package danilov.roman.telegramBot;

import com.google.common.io.Files;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;

public class Bot extends AbilityBot {

    private final String pathToSaveImage = "/home/rdanilov/java/zebrainseye/telegram/";
    private final String pathToImageFromService = "/home/rdanilov/java/zebrainseye/out/";


    protected Bot(String botToken, String botUsername, DefaultBotOptions options) {
        super(botToken, botUsername, options);
    }

    @Override
    public int creatorId() {
        return 0;
    }

    /**
     * Метод-обработчик поступающих сообщений.
     * @param update объект, содержащий информацию о входящем сообщении
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Message message = update.getMessage();
            //long chatId = message.getChatId();
            // get the last photo - it seems to be the bigger one
            List<PhotoSize> photos = message.getPhoto();
            PhotoSize photo = photos.get(photos.size() - 1);
            String id = photo.getFileId();
            try {
                GetFile getFile = new GetFile();
                getFile.setFileId(id);
                File file = execute(getFile);
                String filePath = file.getFileUrl(getBotToken());
                String imageFormat = Files.getFileExtension(filePath);
                String uploadImage = pathToSaveImage + id + "." + imageFormat;

                SendChatAction sendChatAction = new SendChatAction();
                sendChatAction.setChatId(update.getMessage().getChatId());
                sendChatAction.setAction(ActionType.TYPING);
                execute(sendChatAction);

                if (FileUtil.saveFile(filePath, uploadImage)) {

                    Map<String, Object> responseObject = FileUtil.sendFileToServiceAndGetResponse(uploadImage);

                    if (responseObject.containsKey("status")) {
                        String status = (String) responseObject.get("status");
                        if (status.contains("error")) {
                            SendMessage snd = new SendMessage();
                            snd.setChatId(update.getMessage().getChatId());
                            snd.setText((String) responseObject.get("description"));
                            execute(snd);
                        } else {
                            SendPhoto snd = new SendPhoto();
                            snd.setChatId(update.getMessage().getChatId());
                            java.io.File out = new java.io.File(pathToImageFromService + responseObject.get("url"));
                            snd.setPhoto(out);
                            execute(snd);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


//    public Ability hello() {
//        return Ability.builder()
//                .name("test")
//                .info("hello bot")
//                .locality(ALL)
//                .privacy(PUBLIC)
//                .action(ctx -> silent.send("hello!", ctx.chatId()))
//                .build();
//    }
}
