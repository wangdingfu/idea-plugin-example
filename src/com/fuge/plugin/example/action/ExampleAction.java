package com.fuge.plugin.example.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

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
                    PsiTypeElement parameterElement = parameter.getTypeElement();
                    PsiClass classElement = JavaPsiFacade.getInstance(parameter.getProject()).findClass(parameterElement.getType().getCanonicalText(), parameterElement.getResolveScope());
                    if (Objects.nonNull(classElement)) {
                        System.out.println(classElement.getText());
                        List<PsiField> fields = getFields(classElement);
                        for (PsiField field : fields) {
                            String text = field.getText();
                            System.out.println(field.getName() + "的内容为:" + text);
                        }
                    }
                }
            }
        }
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
