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

@Table({ tableName: "empleado", timestamps: false })
export class Empleado extends Model {
    @PrimaryKey
    @ForeignKey(() => Usuario)
    @Column(DataType.INTEGER)
    idUsuario!: number;

    @Column({
        type: DataType.STRING(50),
        allowNull: false
    })
    cargo!: string;

    @Column({
        type: DataType.STRING(100),
        allowNull: false
    })
    nombre!: string;

    @Column({
        type: DataType.STRING(15),
        allowNull: false
    })
    documento!: string;

    @Column({
        type: DataType.STRING(15),
        allowNull: false
    })
    telefono!: string;

    @BelongsTo(() => Usuario)
    usuario!: Usuario;
}