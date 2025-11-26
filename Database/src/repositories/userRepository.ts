import { BaseRepository } from "./baseRepository";
import { Usuario } from "../models";
import { TipoUsuario } from "../types/express";

export class UserRepository extends BaseRepository<Usuario>{
    constructor(){
        super(Usuario)
    }

    async findByEmail(correo: string): Promise<Usuario | null>{
        return this.model.findOne({ where: { correo: correo }})
    }

    async findByActivo(activo: boolean): Promise<Usuario[]>{
        return this.model.findAll({ where: {activo: activo}})
    }

    async findByUserType(tipo: TipoUsuario | string): Promise<Usuario[]>{
        return this.model.findAll({ where: {tipoUsuario: tipo}})
    }
}