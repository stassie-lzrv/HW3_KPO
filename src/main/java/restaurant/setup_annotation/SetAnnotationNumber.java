package restaurant.setup_annotation;

import restaurant.config.AgentJade;

import java.lang.annotation.Annotation;

public interface SetAnnotationNumber {
    default void setNumber(int number){
        try {
            final AgentJade oldAnnotation = (AgentJade) getClass().getAnnotations()[0];
            Annotation newAnnotation = new AgentJade() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return oldAnnotation.annotationType();
                }
                @Override
                public int number() {
                    return number;
                }
                @Override
                public String value() {
                    return "";
                }
            };
            AnnotationHelper.alterAnnotationOn(getClass(), AgentJade.class,newAnnotation);
        } catch (Exception ex){
            System.out.println("Can't set number of agents.");
        }
    }

}

