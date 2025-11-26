import { BaseRepository } from "./baseRepository";
import { ManejadorTokens } from "../models";

export class ManejadorTokensRepository extends BaseRepository<ManejadorTokens> {
    constructor() {
        super(ManejadorTokens);
    }

    async findByToken(token: string): Promise<ManejadorTokens | null> {
        return this.model.findOne({ where: { token } });
    }

    async findByUsuario(idUsuario: number): Promise<ManejadorTokens[]> {
        return this.model.findAll({ where: { idUsuario } });
    }
}
