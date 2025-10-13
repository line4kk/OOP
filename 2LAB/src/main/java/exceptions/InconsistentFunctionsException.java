package exceptions;

public class InconsistentFunctionsException extends  RuntimeException{
    public InconsistentFunctionsException(){
        super();
    }

    public InconsistentFunctionsException(String error){
        super(error);
    }
}
