package com.smilehacker.quicker.data.model;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

/**
 * Created by kleist on 14-4-5.
 */
@Table("package")
public class PackageModel extends Model{

    @AutoIncrementPrimaryKey
    @Column("id")
    public long id;

    @Column("name")
    public String name;

    @Column("json")
    public String json;
}
