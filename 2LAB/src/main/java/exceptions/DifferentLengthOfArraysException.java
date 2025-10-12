package exceptions;

public class DifferentLengthOfArraysException extends  RuntimeException{
    public DifferentLengthOfArraysException(){
        super();
    }

    public DifferentLengthOfArraysException(String error){
        super(error);
    }
}
