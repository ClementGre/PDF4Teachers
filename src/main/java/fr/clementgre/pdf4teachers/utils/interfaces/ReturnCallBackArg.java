package fr.clementgre.pdf4teachers.utils.interfaces;

public interface ReturnCallBackArg<P, R> {
    R call(P value);
}
