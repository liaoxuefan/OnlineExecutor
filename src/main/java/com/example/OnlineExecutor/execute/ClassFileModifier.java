package com.example.OnlineExecutor.execute;

public class ClassFileModifier {
    /**
     * Class文件中常量池的起始偏移
     */
    private static final int CONSTANT_POOL_COUNT_INDEX =8;

    /**
     * CONSTANT_UTF8_INFO常量的tag
     */
    private static final int CONSTANT_UTF8_INFO =1;

    /**
     * 常量池中11中常量的长度，CONSTANT_ITEM_LENGTH[tag]表示它的长度
     */
    private static final int[] CONSTANT_ITEM_LENGTH ={-1,-1,-1,5,5,9,9,3,3,5,5,5,5};

    /**
     * 1个和2个字节的符号数，用来在classByte数组中取tag和len
     * tag用u1个字节表示
     * len用u2个字节表示
     */
    private static final int u1=1;
    private static final int u2=2;

    /**
     * 要被修改的字节码文件
     */
    private byte[] classByte;

    public ClassFileModifier(byte[] classByte){
        this.classByte=classByte;
    }

    /**
     * 从0x00000008开始向后取2个字节，表示的是常量池中常量的个数
     * @return 常量池的常量的个数
     */
    public int getConstantPoolCount(){
        return ByteUtils.byte2Int(classByte,CONSTANT_POOL_COUNT_INDEX,u2);
    }

    /**
     * 字节码修改器，替换字节码常量池中oldStr为newStr
     * @param oldStr
     * @param newStr
     * @return 修改后的字节码字节数组
     */
    public byte[] modifyUTF8Constant(String oldStr,String newStr){
        int cpc=getConstantPoolCount();
        int offset=CONSTANT_POOL_COUNT_INDEX+u2;
        for(int i=1;i<cpc;i++){
            int tag=ByteUtils.byte2Int(classByte,offset,u1);
            if(tag==CONSTANT_UTF8_INFO){
                int len=ByteUtils.byte2Int(classByte,offset+u1,u2);//得到当前常量的数据区
                offset +=u1+u2;
                String str=ByteUtils.byte2String(classByte,offset,len);//得到数据区的字符串
                if(str.equals(oldStr)){
                    byte[] strReplaceBytes=ByteUtils.string2Byte(newStr);
                    byte[] intReplaceBytes=ByteUtils.int2Byte(strReplaceBytes.length,u2);
                    //把旧的长度替换成新的字符串的长度
                    classByte=ByteUtils.byteReplace(classByte,offset-u2,u2,intReplaceBytes);
                    //替换字符串本身
                    classByte=ByteUtils.byteReplace(classByte,offset,len,strReplaceBytes);
                    return classByte;
                }else{
                    offset +=len;
                }
            }else{
                offset+=CONSTANT_ITEM_LENGTH[tag];
            }
        }
        return classByte;
    }
}
