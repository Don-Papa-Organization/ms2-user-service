import { BaseRepository } from "./baseRepository";
import { Cliente } from "../models";

export class ClienteRepository extends BaseRepository<Cliente>{
    constructor(){
        super(Cliente)
    }
}