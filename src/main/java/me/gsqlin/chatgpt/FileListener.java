package me.gsqlin.chatgpt;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件监听示例，在main方法中注明了调用方法。
 * 该类实现FileAlterationListener接口，完善“收听者”类，对收听到的文件变动做自定义处理
 */
public class FileListener implements FileAlterationListener {
    private ChatGPT plugin = ChatGPT.getInstance();
    private String fileName;
    private FileAlterationMonitor monitor;
    private BukkitRunnable runnable;
    private Runnable run;
    public static List<FileAlterationMonitor> monitors = new ArrayList<>();

    public FileListener(String folder,String fileName,Runnable run){
        this.run = run;
        this.fileName = fileName;
        long intervalTime = 1;
        FileAlterationObserver observer = new FileAlterationObserver(folder);

        observer.addListener(this);
        monitor = new FileAlterationMonitor(intervalTime, observer);
        monitors.add(monitor);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    monitor.start();
                    plugin.getLogger().info("§a开始监测文件夹："+ folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        this.runnable = runnable;
        runnable.runTaskAsynchronously(ChatGPT.getInstance());
    }

    @Override
    public void onStart(FileAlterationObserver observer) {
    }

    @Override
    public void onDirectoryCreate(File directory) {
        /* 开启监听后发现有新目录被创建时需要做的事情 ；
         * 但实际测试过程中发现，已有目录被更改时（包括自身文件夹名称更改、下级文件夹更改、下级文件更改），也会触发本方法 ；
         */
    }

    @Override
    public void onDirectoryChange(File directory) {
        /* 开启监听后发现有目录被更改时（包括自身文件夹名称更改、下级文件夹更改、下级文件更改）需要做的事情 ；
         * 但实际测试过程中发现，更改文件夹名称时，会同时触发文件夹被删除、文件夹新增事件 ；
         */
    }

    @Override
    public void onDirectoryDelete(File directory) {
        /* 开启监听后发现有目录被删除时需要做的事情 ；
         * 但实际测试过程中发现，已有目录被更改时（包括自身文件夹名称更改、下级文件夹更改、下级文件更改），也会触发本方法
         */
    }

    @Override
    public void onFileCreate(File file) {
        /* 开启监听后发现新增文件时需要做的事情 ；
         */
    }

    @Override
    public void onFileChange(File file) {
        /* 开启监听后发现有文件夹被修改时（包括文件名修改、文件内容修改）需要做的事情 ；
         */
        if (!file.getName().equalsIgnoreCase(fileName)) return;
        run.run();
    }

    @Override
    public void onFileDelete(File file) {
        /* 开启监听后发现有文件夹被删除时需要做的事情 ；
         */
    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }

    public FileAlterationMonitor getMonitor() {
        return monitor;
    }

    public BukkitRunnable getRunnable() {
        return runnable;
    }

    public void setRun(Runnable run) {
        this.run = run;
    }
}
