import { Request, Response } from 'express';
import { BaseController } from './baseController';
import { Empleado } from '../models';
import { EmpleadoRepository } from '../repositories/employerRepository';

export class EmpleadoController extends BaseController<Empleado> {
    private empleadoRepository: EmpleadoRepository;

    constructor() {
        const empleadoRepo = new EmpleadoRepository();
        super(empleadoRepo);
        this.empleadoRepository = empleadoRepo;
    }

    async getByCargo(req: Request, res: Response): Promise<void> {
        try {
            const { cargo } = req.params;
            
            if (!cargo || typeof cargo !== 'string') {
                res.status(400).json({
                    success: false,
                    error: 'Cargo es requerido y debe ser texto'
                });
                return;
            }

            const empleados = await this.empleadoRepository.findByEmployerPosition(cargo);
            
            empleados.length > 0
                ? res.json({ success: true, data: empleados })
                : res.status(404).json({
                    success: false,
                    error: 'No se encontraron empleados con ese cargo'
                });
        } catch (error) {
            this.handleError(error, res, 'Error al buscar empleados por cargo');
        }
    }
}