package com.a50647.hotfixdemo;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 热修复帮助类
 *
 * @author wm
 * @date 2019/2/28
 */
public class HotFixUtils {
    private static List<File> mList = new ArrayList<>();
    private static final String OUT_PUT_DIR = "optimizedDirectory";

    /**
     * 加载补丁
     *
     * @param context 上下文
     * @param dexDir  补丁所存文件夹
     */
    public static void loadFixedDex(Context context, File dexDir) {
        if (context == null) {
            return;
        }
        context = context.getApplicationContext();
        //寻找补丁
        findPatch(dexDir);
        if (mList.size() == 0) {
            return;
        }
        //创建优化缓存路径
        File optimizeDir = makeOptimizeDir(context);
        //获取重组后的dexPath,用":"分割
        StringBuilder dexPathBuilder;
        if (mList.size() == 1) {
            dexPathBuilder = new StringBuilder(mList.get(0).getAbsolutePath());
        } else {
            dexPathBuilder = new StringBuilder(mList.get(0).getAbsolutePath());
            for (int i = 1; i < mList.size(); i++) {
                dexPathBuilder.append(":").append(mList.get(i).getAbsolutePath());
            }
        }
        String dexPath = dexPathBuilder.toString();
        PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();
        //创建DexClassLoader
        DexClassLoader dexClassLoader = new DexClassLoader(
                dexPath
                , optimizeDir.getAbsolutePath()
                , null
                , pathLoader);
        try {
            //通过反射,分别获取pathLoader以及dexClassLoader当中的成员变量DexPathList
            Object dexElements = getElements(dexClassLoader);
            Object pathElements = getElements(pathLoader);
            //将通过dexClassLoader获取的数组添加到通过pathList获取到的数组之前
            Class<?> componentType = pathElements.getClass().getComponentType();
            int i = Array.getLength(dexElements);
            int j = Array.getLength(pathElements);
            int k = i + j;
            Object newPathElements = Array.newInstance(componentType, k);
            System.arraycopy(dexElements, 0, newPathElements, 0, i);
            System.arraycopy(pathElements, 0, newPathElements, i, j);
            //重新赋值
            Object pathList = getField(pathLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
            setField(pathList, pathList.getClass(), "dexElements", newPathElements);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 寻找补丁
     *
     * @param dexDir 补丁所存放的文件夹
     */
    private static void findPatch(File dexDir) {
        mList.clear();
        File[] listFiles = dexDir.listFiles();
        for (File file : listFiles) {
            //找到补丁
            if (file.getName().startsWith("classes") && (file.getName().endsWith(".dex"))) {
                mList.add(file);
            }
        }
    }

    /**
     * 创建优化缓存路径
     *
     * @param context application的context
     */
    private static File makeOptimizeDir(Context context) {
        String optimizeDirName = context.getFilesDir().getAbsolutePath() + "/" + OUT_PUT_DIR;
        File optimizeDir = new File(optimizeDirName);
        if (!optimizeDir.exists()) {
            optimizeDir.mkdirs();
        }
        return optimizeDir;
    }

    /**
     * 通过反射
     * 1.获取BaseDexClassLoader中的DexPathList
     * 2.获取DexPathList中的Element[];
     */
    private static Object getElements(Object classLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Object pathList = getField(classLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    /**
     * 反射给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }
}
