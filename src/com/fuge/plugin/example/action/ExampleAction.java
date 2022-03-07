package com.fuge.plugin.example.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExampleAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        PsiClass psiClass = getPsiClass(e);
        if (Objects.nonNull(psiClass)) {
            System.out.println(psiClass.getText());
            PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                PsiParameterList parameterList = method.getParameterList();
                PsiParameter[] parameters = parameterList.getParameters();
                for (PsiParameter parameter : parameters) {
                    PsiClass classElement = convert(parameter.getProject(), parameter.getTypeElement());
                    List<String> strings = recursiveObject(classElement);
                    System.out.println(strings);
                }
            }
        }
    }


    private List<String> recursiveObject(PsiClass classElement) {
        List<String> fileNameList = Lists.newArrayList();
        if (Objects.nonNull(classElement)) {
            String text = classElement.getText();
            System.out.println("正在递归" + classElement.getName() + "对象.对象内容为:\r\n" + text);
            List<PsiField> fields = getFields(classElement);
            for (PsiField field : fields) {
                String fieldText = field.getText();
                System.out.println("正在解析" + field.getName() + "字段. 字段内容为:\r\n" + fieldText);
                PsiType type = field.getType();
                String canonicalText = type.getCanonicalText();
                System.out.println("字段【" + field.getName() + "】的类型为:" + canonicalText);
                switch (canonicalText) {
                    case "java.lang.String":
                    case "java.lang.Integer":
                    case "java.math.BigDecimal":
                    case "java.lang.Double":
                    case "java.lang.Float":
                    case "java.lang.Long":
                    case "java.lang.Short":
                        fileNameList.add(field.getName() + "的类型为:" + canonicalText);
                        break;
                    case "java.util.List":
                        //是list集合 需要特殊处理
                        System.out.println(field.getName() + "字段是list 暂时无法遍历");
                        break;
                    default:
                        //是其他对象 则需要便利
                        fileNameList.addAll(recursiveObject(convert(field.getProject(), field.getTypeElement())));
                }
            }
        }
        return fileNameList;
    }


    public PsiClass convert(Project project, PsiTypeElement psiTypeElement) {
        return JavaPsiFacade.getInstance(project).findClass(psiTypeElement.getType().getCanonicalText(), psiTypeElement.getResolveScope());

    }

    /**
     * 获取类的所有属性
     *
     * @param e
     * @return
     */
    public static List<PsiField> getFields(AnActionEvent e) {
        PsiClass clazz = getPsiClass(e);
        return getFields(clazz);
    }

    public static List<PsiField> getFields(PsiClass clazz) {
        List<PsiField> result = new ArrayList<>();
        PsiField[] fields = clazz.getFields();
        for (PsiField field : fields) {
            String fieldName = field.getName();
            result.add(field);
        }
        return result;
    }


    public static boolean isInterface(AnActionEvent e) {
        PsiClass clazz = getPsiClass(e);
        return clazz.isInterface();
    }

    public static PsiMethod[] getMethod(String getMethodName, AnActionEvent e) {
        PsiClass clazz = getPsiClass(e);
        PsiMethod[] methods = clazz.findMethodsByName(getMethodName, true);
        //boolean isPublic = field.hasModifierProperty(PsiModifier.PUBLIC);
        //if (isPublic) { return true; }
        return methods;
    }


    public static PsiClass getPsiClass(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    protected PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }
}
