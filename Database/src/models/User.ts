import {
    Table,
    Column,
    Model,
    DataType,
    PrimaryKey,
    AutoIncrement,
} from "sequelize-typescript"
import { TipoUsuario } from "../types/express";

@Table({ tableName: "usuario", timestamps: false})
export class Usuario extends Model {
    @PrimaryKey
    @AutoIncrement
    @Column(DataType.INTEGER)
    idUsuario!: number;

    @Column({
        type: DataType.STRING,
        allowNull: false,
        unique: true,
        validate: {
            isEmail: true
        }
    })
    correo!: string;

    @Column({
        type: DataType.STRING,
        allowNull: false
    })
    contrasena!: string;

    @Column({
        type: DataType.ENUM('cliente', 'empleado', 'administrador'),
        allowNull: false
    })
    tipoUsuario!: TipoUsuario;

    @Column({
        type: DataType.BOOLEAN,
        allowNull: false
    })
    activo!: boolean


}