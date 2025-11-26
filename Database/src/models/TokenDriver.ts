import {
    Table,
    Column,
    PrimaryKey,
    ForeignKey,
    BelongsTo,
    DataType,
    AutoIncrement,
    Model
} from "sequelize-typescript"
import { Usuario } from "./User";

@Table({ tableName: "manejadorTokens", timestamps: false})
export class ManejadorTokens extends Model{
    @PrimaryKey
    @AutoIncrement
    @Column(DataType.INTEGER)
    idManejadorTokens!: number

    @Column({
        type: DataType.DATE,
        allowNull: false,
        defaultValue: DataType.NOW
    })
    creadoEn!: Date;

    @Column({
        type: DataType.DATE,
        allowNull: false
    })
    expiraEn!: Date;

    @Column({
        type: DataType.STRING,
        allowNull: false
    })
    token!: string;

    @ForeignKey(() => Usuario)
    @Column(DataType.INTEGER)
    idUsuario!: number;

    @BelongsTo(() => Usuario)
    usuario!: Usuario
}
