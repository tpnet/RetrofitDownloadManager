package com.tpnet.retrofitdownloaddemo;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.RowMapper;

/**
 * 要下载的数据bean
 * Created by litp on 2017/4/18.
 */

@AutoValue
public abstract class Program implements Parcelable,ProgramModel{


   

    public static final Factory<Program> FACTORY = new Factory<>(new ProgramModel.Creator<Program>() {
        @Override
        public Program create(int _id, @Nullable String downLink, @Nullable String name) {
            return new AutoValue_Program(_id,downLink,name);
        }
    });
    
    
    

    public static Program create(String downLink, String name) {
        return builder()
                ._id(0)
                .downLink(downLink)
                .name(name)
                .build();
    }
    
    
    public static Program create(Program program) {
        return builder()
                .downLink(program.downLink())
                .name(program.name())
                .build();
    }
    
    
    public static Builder builder() {
        return new AutoValue_Program.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder _id(int _id);

        public abstract Builder downLink(String downLink);

        public abstract Builder name(String name);

        public abstract Program build();
    }


    //查询title映射
    public final static RowMapper<String> ROW_NAMW_MAPPER = FACTORY.selectDownNameMapper();

    
    
    


}
