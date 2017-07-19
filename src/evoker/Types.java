package evoker;

public class Types {

    public enum FileFormat {
        DEFAULT, OXFORD, UKBIOBANK
    }

    public enum CoordinateSystem {
        CART, POLAR, UKBIOBANK
    }

    public enum SortBy {
        COLLECTIONBATCH_ASCEND, COLLECTIONBATCH_DESCEND, MAF_ASCEND, MAF_DESCEND, GPC_ASCEND, GPC_DESCEND, HWEPVAL_ASCEND, HWEPVAL_DESCEND
    }

    public enum Sex {
        MALE, FEMALE, UNKNOWN, NOT_SEX
    }

}
