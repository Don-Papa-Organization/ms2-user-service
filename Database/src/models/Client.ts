import {
    Table,
    Column,
    Model,
    DataType,
    PrimaryKey,
    ForeignKey,
    BelongsTo
} from "sequelize-typescript";
import { Usuario } from "./User";

@Table({ tableName: "cliente", timestamps: false })
export class Cliente extends Model {
    @PrimaryKey
    @ForeignKey(() => Usuario)
    @Column(DataType.INTEGER)
    idUsuario!: number;

    @Column({
        type: DataType.STRING(200),
        allowNull: false
    })
    direccion!: string;

    @Column({
        type: DataType.STRING(100),
        allowNull: false
    })
    nombre!: string;

    @Column({
        type: DataType.STRING(20),
        allowNull: false
    })
    telefono!: string;

    @BelongsTo(() => Usuario)
     usuario!: Usuario;
}