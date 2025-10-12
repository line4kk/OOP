package exceptions;

public class InterpolationException extends  RuntimeException{
    public InterpolationException(){
        super();
    }

    public InterpolationException(String error){
        super(error);
    }
}
