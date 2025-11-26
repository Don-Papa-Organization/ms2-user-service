import { Request, Response } from 'express';
import { BaseController } from './baseController';
import { Cliente } from '../models';
import { ClienteRepository } from '../repositories/clientRepository';

export class ClienteController extends BaseController<Cliente> {
    private clienteRepository: ClienteRepository;

    constructor() {
        const clienteRepo = new ClienteRepository();
        super(clienteRepo);
        this.clienteRepository = clienteRepo;
    }
}