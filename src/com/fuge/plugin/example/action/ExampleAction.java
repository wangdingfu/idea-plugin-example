package com.fuge.plugin.example.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
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
            PsiModifierList modifierList1 = psiClass.getModifierList();
            PsiAnnotation[] annotations1 = modifierList1.getAnnotations();
            System.out.println(annotations1.toString());
            for (PsiMethod method : methods) {
                PsiModifierList modifierList = method.getModifierList();
                PsiAnnotation[] annotations = modifierList.getAnnotations();
                System.out.println(annotations.toString());
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
            String qualifiedName = classElement.getQualifiedName();
            if(Objects.nonNull(qualifiedName) && jclx(qualifiedName)){
                fileNameList.add(classElement.getName() + "????????????:" + qualifiedName);
                return fileNameList;
            }
            String text = classElement.getText();
            System.out.println("????????????" + classElement.getName() + "??????.???????????????:\r\n" + text);

            List<PsiField> fields = getFields(classElement);
            for (PsiField field : fields) {
                String fieldText = field.getText();
                System.out.println("????????????" + field.getName() + "??????. ???????????????:\r\n" + fieldText);
                PsiType type = field.getType();
                String canonicalText = type.getCanonicalText();
                PsiClass psiClass = PsiUtil.resolveClassInType(type);
                if(Objects.isNull(psiClass)){
                    throw new RuntimeException("psiClass is???null");
                }
                qualifiedName = psiClass.getQualifiedName();
                if(Objects.isNull(qualifiedName)){
                    throw new RuntimeException("qualifiedName is null");
                }
                System.out.println("?????????" + field.getName() + "???????????????:" + qualifiedName);
                switch (qualifiedName) {
                    case "java.lang.String":
                    case "java.lang.Integer":
                    case "java.math.BigDecimal":
                    case "java.lang.Double":
                    case "java.lang.Float":
                    case "java.lang.Long":
                    case "java.lang.Short":
                        fileNameList.add(field.getName() + "????????????:" + qualifiedName);
                        break;
                    case "java.util.List":
                        //???list?????? ??????????????????
                        System.out.println(field.getName() + "?????????list ??????????????????");
                        //??????Java?????????PsiType??????
                        PsiType iterableType = PsiUtil.extractIterableTypeParameter(type, false);
                        //??????Type???PsiClass
                        PsiClass iterableClass = PsiUtil.resolveClassInClassTypeOnly(iterableType);
                        fileNameList.addAll(recursiveObject(iterableClass));
                        break;
                    default:
                        //??????????????? ???????????????
                        fileNameList.addAll(recursiveObject(convert(field.getProject(), field.getTypeElement())));
                }
            }
        }
        return fileNameList;
    }


    private boolean jclx(String type){
        switch (type){
            case "java.lang.String":
            case "java.lang.Integer":
            case "java.math.BigDecimal":
            case "java.lang.Double":
            case "java.lang.Float":
            case "java.lang.Long":
            case "java.lang.Short":
                return true;
        }
        return false;
    }


    public PsiClass convert(Project project, PsiTypeElement psiTypeElement) {
        return JavaPsiFacade.getInstance(project).findClass(psiTypeElement.getType().getCanonicalText(), psiTypeElement.getResolveScope());

    }

    /**
     * ????????????????????????
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
