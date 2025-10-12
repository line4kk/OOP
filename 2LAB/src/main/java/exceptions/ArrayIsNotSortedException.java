package exceptions;

public class ArrayIsNotSortedException extends  RuntimeException{
    public ArrayIsNotSortedException(){
        super();
    }

    public ArrayIsNotSortedException(String error){
        super(error);
    }
}
