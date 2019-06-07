package com.geekbrains.mycloud.server;

import com.geekbrains.mycloud.common.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.TreeMap;

//import javafx.scene.shape.Path;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ServerApp.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileRequest) { // сли получаем от клиента сообщение типа FileRequest с именем файла fr.getFilename(),
                FileRequest fr = (FileRequest) msg; // то если в папке "server_storage/" находится
                if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) { // файл с именем fr.getFilename()

                    FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename())); // посылаем клиенту этот файл
                    ctx.writeAndFlush(fm);
                }
            } else if (msg instanceof FileMessage) { // если получаем от клиента сообщение типа FileMessage с файлом ,
                // то записываем его с именем fm.getFilename() в папку "server_storage/"

                FileMessage fm = (FileMessage) msg;
                byte[] buffer = fm.getData();
                ByteBufAllocator al = new PooledByteBufAllocator();
                ByteBuf buf = al.directBuffer(buffer.length);

                buf.writeBytes(buffer);      // direct buffer

                FileOutputStream fileOutputStream = new FileOutputStream("server_storage/" + fm.getFilename());
                fileOutputStream.write(buffer);
                fileOutputStream.close();


            }   //если получаем сообщение с логином/паролем типа AuthMessage, то либо продолжаем работу
            // либо не пускаем дальше
            else if (msg instanceof AuthMessage) {
                AuthMessage message = (AuthMessage) msg;
                AuthService.connect();
                if (AuthService.checkUser(message.getLogin())) {
                    if (AuthService.checkPassword(message.getLogin(), message.getPassword())) {
                        ctx.writeAndFlush("User checked/" + message.getLogin());
                        logger.info("Correct User");
                    } else {
                        ctx.writeAndFlush("Enter correct password");
                        logger.info("Password Error!");
                    }
                } else {
                    ctx.writeAndFlush("Enter correct user");
                    logger.info("User doesn't exist!");
                }
            }
            // если получаем требование (типа RegMessage) зарегистрировать нового пользователя ,
            // то либо регистрируем, либо отвечаем, что такой пользователь уже зарегистрирован
            else if (msg instanceof RegMessage) {
                RegMessage message = (RegMessage) msg;
                AuthService.connect();
                if (AuthService.checkUser(message.getLogin())) {
                    ctx.writeAndFlush("User already registered");
                    logger.info("User already registered");
                } else {
                    AuthService.addUser(message.getLogin(), message.getPassword());
                    ctx.writeAndFlush("Correct registration");
                    logger.info("Correct registration");
                }

            } else if (msg instanceof FileDeleteMessage) {
                FileDeleteMessage fileDeleteMessage = (FileDeleteMessage) msg;
                Files.delete(Paths.get("server_storage/" + fileDeleteMessage.getFileName()));
                System.out.println("server_storage/" + fileDeleteMessage.getFileName());
            } else if (msg instanceof RefreshMessage) {
                RefreshMessage refreshMessage = (RefreshMessage) msg;
                if (refreshMessage.getRefresh().equals("refresh")) {

                    Files.walkFileTree(Paths.get("server_storage/"), EnumSet.noneOf(FileVisitOption.class), 2, new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                            TreeMap<String, Long> findFiles = new TreeMap<>();
                            String fileName = file.getFileName().toString();
                            Long fileSize = file.toFile().length();
                            findFiles.put(fileName, fileSize);
                            RefreshSrvFileListMessage refreshSrvFileListMessage = new RefreshSrvFileListMessage(findFiles);
                            ctx.writeAndFlush(refreshSrvFileListMessage);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.warn("Connection Error/disconnect");
        ctx.close();
    }
}

