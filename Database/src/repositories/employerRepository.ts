import { BaseRepository } from "./baseRepository";
import { Empleado } from "../models";

export class EmpleadoRepository extends BaseRepository<Empleado> {
    constructor() {
        super(Empleado)
    }

    async findByEmployerPosition(cargo: string): Promise<Empleado[]> {
        return this.model.findAll({ where: { cargo: cargo } })
    }
}