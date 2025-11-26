import { Request, Response } from 'express';
import { BaseController } from './baseController';
import { ManejadorTokens } from '../models';
import { ManejadorTokensRepository } from '../repositories/manejadorTokensRepository';

export class ManejadorTokensController extends BaseController<ManejadorTokens> {
    private manejadorTokensRepository: ManejadorTokensRepository;

    constructor() {
        const manejadorTokensRepo = new ManejadorTokensRepository();
        super(manejadorTokensRepo);
        this.manejadorTokensRepository = manejadorTokensRepo;
    }

    async getByToken(req: Request, res: Response): Promise<void> {
        try {
            const { token } = req.params;
            
            if (!token || typeof token !== 'string') {
                res.status(400).json({
                    success: false,
                    error: 'Token es requerido y debe ser texto'
                });
                return;
            }

            const manejadorToken = await this.manejadorTokensRepository.findByToken(token);
            
            manejadorToken
                ? res.json({ success: true, data: manejadorToken })
                : res.status(404).json({
                    success: false,
                    error: 'Token no encontrado'
                });
        } catch (error) {
            this.handleError(error, res, 'Error al buscar token');
        }
    }

    async getByUsuario(req: Request, res: Response): Promise<void> {
        try {
            const idUsuario = this.validateId(req.params.idUsuario);
            
            if (!idUsuario) {
                res.status(400).json({
                    success: false,
                    error: 'ID de usuario inválido'
                });
                return;
            }

            const tokens = await this.manejadorTokensRepository.findByUsuario(idUsuario);
            
            tokens.length > 0
                ? res.json({ success: true, data: tokens })
                : res.status(404).json({
                    success: false,
                    error: 'No se encontraron tokens para este usuario'
                });
        } catch (error) {
            this.handleError(error, res, 'Error al buscar tokens del usuario');
        }
    }
}